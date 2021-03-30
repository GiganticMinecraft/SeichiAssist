package com.github.unchama.fs2.workaround

import cats.effect.Concurrent
import cats.effect.concurrent.{Deferred, Ref}
import cats.implicits._
import fs2.Stream._
import fs2.concurrent.{InspectableQueue, Signal, SignallingRef}
import fs2.{Sink, Stream}

/*
Code directly copied from https://github.com/typelevel/fs2/blob/4ddd75a2dc032b7604dc1205c86d7d6adc993859/core/shared/src/main/scala/fs2/concurrent/Topic.scala.
This is due to https://github.com/typelevel/fs2/issues/1406, and the recommended workaround
was to switch back to an old implementation of Topic which is better in terms of performance.

The MIT License (MIT)

Copyright (c) 2013 Paul Chiusano, and respective contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

/**
 * Asynchronous Topic.
 *
 * Topic allows you to distribute `A` published by arbitrary number of publishers to arbitrary number of subscribers.
 *
 * Topic has built-in back-pressure support implemented as maximum bound (`maxQueued`) that a subscriber is allowed to enqueue.
 * Once that bound is hit, publishing may semantically block until the lagging subscriber consumes some of its queued elements.
 *
 * Additionally the subscriber has possibility to terminate whenever size of enqueued elements is over certain size
 * by using `subscribeSize`.
 */
abstract class Topic[F[_], A] {
  self =>

  /**
   * Publishes elements from source of `A` to this topic.
   * [[Sink]] equivalent of `publish1`.
   */
  def publish: Sink[F, A]

  /**
   * Publishes one `A` to topic.
   *
   * This waits until `a` is published to all subscribers.
   * If any of the subscribers is over the `maxQueued` limit, this will wait to complete until that subscriber processes
   * enough of its elements such that `a` is enqueued.
   */
  def publish1(a: A): F[Unit]

  /**
   * Subscribes for `A` values that are published to this topic.
   *
   * Pulling on the returned stream opens a "subscription", which allows up to
   * `maxQueued` elements to be enqueued as a result of publication.
   *
   * The first element in the stream is always the last published `A` at the time
   * the stream is first pulled from, followed by each published `A` value from that
   * point forward.
   *
   * If at any point, the queue backing the subscription has `maxQueued` elements in it,
   * any further publications semantically block until elements are dequeued from the
   * subscription queue.
   *
   * @param maxQueued maximum number of elements to enqueue to the subscription
   *                  queue before blocking publishers
   */
  def subscribe(maxQueued: Int): Stream[F, A]

  /**
   * Like [[subscribe]] but emits an approximate number of queued elements for this subscription
   * with each emitted `A` value.
   */
  def subscribeSize(maxQueued: Int): Stream[F, (A, Int)]

  /**
   * Signal of current active subscribers.
   */
  def subscribers: Signal[F, Int]

  /**
   * Returns an alternate view of this `Topic` where its elements are of type `B`,
   * given two functions, `A => B` and `B => A`.
   */
  def imap[B](f: A => B)(g: B => A): Topic[F, B] =
    new Topic[F, B] {
      def publish: Sink[F, B] = sfb => self.publish(sfb.map(g))

      def publish1(b: B): F[Unit] = self.publish1(g(b))

      def subscribe(maxQueued: Int): Stream[F, B] =
        self.subscribe(maxQueued).map(f)

      def subscribers: Signal[F, Int] = self.subscribers

      def subscribeSize(maxQueued: Int): Stream[F, (B, Int)] =
        self.subscribeSize(maxQueued).map { case (a, i) => f(a) -> i }
    }
}

object Topic {

  def apply[F[_], A](initial: A)(implicit F: Concurrent[F]): F[Topic[F, A]] = {
    // Id identifying each subscriber uniquely
    class ID

    sealed trait Subscriber {
      def publish(a: A): F[Unit]

      def id: ID

      def subscribe: Stream[F, A]

      def subscribeSize: Stream[F, (A, Int)]

      def unSubscribe: F[Unit]
    }

    Ref
      .of[F, (A, Vector[Subscriber])]((initial, Vector.empty[Subscriber]))
      .flatMap { state =>
        SignallingRef[F, Int](0).map { subSignal =>
          def mkSubscriber(maxQueued: Int): F[Subscriber] =
            for {
              q <- InspectableQueue.bounded[F, A](maxQueued)
              firstA <- Deferred[F, A]
              done <- Deferred[F, Boolean]
              sub = new Subscriber {
                def unSubscribe: F[Unit] =
                  for {
                    _ <- state.update {
                      case (a, subs) => a -> subs.filterNot(_.id == id)
                    }
                    _ <- subSignal.update(_ - 1)
                    _ <- done.complete(true)
                  } yield ()

                def subscribe: Stream[F, A] = eval(firstA.get) ++ q.dequeue

                def publish(a: A): F[Unit] =
                  q.offer1(a).flatMap { offered =>
                    if (offered) F.unit
                    else {
                      eval(done.get)
                        .interruptWhen(q.size.map(_ < maxQueued))
                        .last
                        .flatMap {
                          case None => eval(publish(a))
                          case Some(_) => Stream.empty
                        }
                        .compile
                        .drain
                    }
                  }

                def subscribeSize: Stream[F, (A, Int)] =
                  eval(firstA.get).map(_ -> 0) ++ q.dequeue.zip(Stream.repeatEval(q.getSize))

                val id: ID = new ID
              }
              a <- state.modify { case (a, s) => (a, s :+ sub) -> a }
              _ <- subSignal.update(_ + 1)
              _ <- firstA.complete(a)
            } yield sub

          new Topic[F, A] {
            def publish: Sink[F, A] =
              _.flatMap(a => eval(publish1(a)))

            def subscribers: Signal[F, Int] = subSignal

            def publish1(a: A): F[Unit] =
              state.modify {
                case (_, subs) =>
                  (a, subs) -> subs.traverse_(_.publish(a))
              }.flatten

            def subscribe(maxQueued: Int): Stream[F, A] =
              bracket(mkSubscriber(maxQueued))(_.unSubscribe).flatMap(_.subscribe)

            def subscribeSize(maxQueued: Int): Stream[F, (A, Int)] =
              bracket(mkSubscriber(maxQueued))(_.unSubscribe).flatMap(_.subscribeSize)
          }
        }
      }
  }
}
