package com.github.unchama.fs2.workaround.fs3

/**
 * Code backported from fs2 3.x. This is due to https://github.com/typelevel/fs2/issues/1406.
 */

/*
 * Copyright (c) 2013 Functional Streams for Scala
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import cats.effect._
import cats.effect.concurrent.{Deferred, Ref}
import cats.effect.implicits._
import cats.syntax.all._
import fs2.{Chunk, Pipe, Pull, Stream}

/**
 * Stream aware, multiple producer, single consumer closeable channel.
 */
sealed trait Fs3Channel[F[_], A] {

  /**
   * Sends all the elements of the input stream through this channel, and closes it after.
   * Especially useful if the channel is single producer.
   */
  def sendAll: Pipe[F, A, Nothing]

  /**
   * Sends an element through this channel.
   *
   * It can be called concurrently by multiple producers, and it may semantically block if the
   * channel is bounded or synchronous.
   *
   * No-op if the channel is closed, see [[close]] for further info.
   */
  def send(a: A): F[Either[Fs3Channel.Closed, Unit]]

  /**
   * The stream of elements sent through this channel. It terminates if [[close]] is called and
   * all elements in the channel have been emitted (see [[close]] for futher info).
   *
   * This method CANNOT be called concurrently by multiple consumers, if you do so, one of the
   * consumers might become permanently deadlocked.
   *
   * It is possible to call `stream` again once the previous one has terminated, but be aware
   * that some element might get lost in the process, e.g if the first call to `stream` got 5
   * elements off the channel, and terminated after emitting 2, when the second call to `stream`
   * starts it won't see those 3 elements.
   *
   * Every time `stream` is pulled, it will serve all the elements that are queued up in a
   * single chunk, including those from producers that might be semantically blocked on a
   * bounded channel, which will then become unblocked. That is, a bound on a channel represents
   * the maximum number of elements that can be queued up before a producer blocks, and not the
   * maximum number of elements that will be received by `stream` at once.
   */
  def stream: Stream[F, A]

  /**
   * This method achieves graceful shutdown: when the channel gets closed, `stream` will
   * terminate naturally after consuming all currently enqueued elements, including the ones by
   * producers blocked on a bound.
   *
   * "Termination" here means that `stream` will no longer wait for new elements on the channel,
   * and not that it will be interrupted while performing another action: if you want to
   * interrupt `stream` immediately, without first processing enqueued elements, you should use
   * `interruptWhen` on it instead.
   *
   * After a call to `close`, any further calls to `send` or `close` will be no-ops.
   *
   * Note that `close` does not automatically unblock producers which might be blocked on a
   * bound, they will only become unblocked if `stream` is executing.
   *
   * In other words, if `close` is called while `stream` is executing, blocked producers will
   * eventually become unblocked, before `stream` terminates and further `send` calls become
   * no-ops. However, if `close` is called after `stream` has terminated (e.g because it was
   * interrupted, or had a `.take(n)`), then blocked producers will stay blocked unless they get
   * explicitly unblocked, either by a further call to `stream` to drain the channel, or by a a
   * `race` with `closed`.
   */
  def close: F[Either[Fs3Channel.Closed, Unit]]

  /**
   * Returns true if this channel is closed
   */
  def isClosed: F[Boolean]

  /**
   * Semantically blocks until the channel gets closed.
   */
  def closed: F[Unit]
}

object Fs3Channel {
  type Closed = Closed.type
  object Closed

  def unbounded[F[_]: Concurrent, A]: F[Fs3Channel[F, A]] =
    bounded(Int.MaxValue)

  def synchronous[F[_]: Concurrent, A]: F[Fs3Channel[F, A]] =
    bounded(0)

  def bounded[F[_], A](capacity: Int)(implicit F: Concurrent[F]): F[Fs3Channel[F, A]] = {
    case class State(
      values: Vector[A],
      size: Int,
      waiting: Option[Deferred[F, Unit]],
      producers: Vector[(A, Deferred[F, Unit])],
      closed: Boolean
    )

    val initial = State(Vector.empty, 0, None, Vector.empty, false)

    (Ref[F].of(initial), Deferred.tryable[F, Unit]).mapN { (state, closedGate) =>
      new Fs3Channel[F, A] {

        def sendAll: Pipe[F, A, Nothing] = { in =>
          (in ++ Stream.eval_(close.void)).evalMap(send).takeWhile(_.isRight).drain
        }

        def send(a: A): F[Either[Closed, Unit]] = {
          Deferred[F, Unit].flatMap { producer =>
            F.uncancelable {
              state.modify {
                case s @ State(_, _, _, _, closed @ true) =>
                  (s, Fs3Channel.closed.pure[F])

                case State(values, size, waiting, producers, closed @ false) =>
                  if (size < capacity)
                    (
                      State(values :+ a, size + 1, None, producers, false),
                      notifyStream(waiting)
                    )
                  else
                    (
                      State(values, size, None, producers :+ (a -> producer), false),
                      // FIXME:
                      //   The latest implementation of fs2.concurrent.Channel would poll on the producer.get
                      //   to allow cancellation of the send action. This is achieved by the MonadCancel typeclass of
                      //   cats-effect 3, which is not available on cats-effect 2.
                      //   We are temporarily not allowing any cancellation to happen here, but this can be improved.
                      notifyStream(waiting) <* producer.get
                    )
              }.flatten
            }
          }
        }

        def close: F[Either[Closed, Unit]] =
          state
            .modify {
              case s @ State(_, _, _, _, closed @ true) =>
                (s, Fs3Channel.closed.pure[F])

              case State(values, size, waiting, producers, closed @ false) =>
                (
                  State(values, size, None, producers, true),
                  notifyStream(waiting) <* signalClosure
                )
            }
            .flatten
            .uncancelable

        def isClosed: F[Boolean] = closedGate.tryGet.map(_.isDefined)

        def closed: F[Unit] = closedGate.get

        def stream: Stream[F, A] = consumeLoop.stream

        def consumeLoop: Pull[F, A, Unit] =
          Pull.eval {
            Deferred[F, Unit].flatMap { waiting =>
              state
                .modify {
                  case State(values, size, ignorePreviousWaiting @ _, producers, closed) =>
                    if (values.nonEmpty || producers.nonEmpty) {
                      var unblock_ = F.unit
                      var allValues_ = values

                      producers.foreach {
                        case (value, producer) =>
                          allValues_ = allValues_ :+ value
                          unblock_ = unblock_ >> producer.complete(()).void
                      }

                      val unblock = unblock_
                      val allValues = allValues_

                      val toEmit = Chunk.vector(allValues)

                      (
                        State(Vector(), 0, None, Vector.empty, closed),
                        // unblock needs to execute in F, so we can make it uncancelable
                        unblock.as(Pull.output(toEmit) >> consumeLoop)
                      )
                    } else {
                      (
                        State(values, size, waiting.some, producers, closed),
                        F.pure(
                          if (closed) Pull.done
                          else (Pull.eval(waiting.get) >> consumeLoop)
                        )
                      )
                    }
                }
                .flatten
                .uncancelable
            }
          }.flatten

        def notifyStream(waitForChanges: Option[Deferred[F, Unit]]): F[Either[Closed, Unit]] =
          waitForChanges.traverse(_.complete(())).as(rightUnit)

        def signalClosure: F[Unit] = closedGate.complete(())
      }
    }
  }

  // allocate once
  private final val closed: Either[Closed, Unit] = Left(Closed)
  private final val rightUnit: Either[Closed, Unit] = Right(())
}
