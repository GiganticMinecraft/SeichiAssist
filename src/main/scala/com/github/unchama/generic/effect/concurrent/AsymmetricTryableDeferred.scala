package com.github.unchama.generic.effect.concurrent

import java.util.concurrent.atomic.AtomicReference

import cats.effect.concurrent.Deferred
import cats.effect.{Concurrent, Sync}

import scala.annotation.tailrec
import scala.collection.immutable.LongMap

/**
 * A [[Deferred]] to which one can query for the "current value".
 *
 * While [[cats.effect.concurrent.TryableDeferred]] serves for this purpose, it restricts
 * the `tryGet` operation to the same context the promise is being completed.
 * This turns out to be too restrictive in some cases, especially when we want to immediately know
 * if the promise is completed or not.
 *
 * [[AsymmetricTryableDeferred]] is similar to [[cats.effect.concurrent.TryableDeferred]] in spirit,
 * but allows one to query for the current value in a more relaxed context.
 */
trait AsymmetricTryableDeferred[F[_], A] extends Deferred[F, A] {

  def tryGet[G[_] : Sync]: G[Option[A]]

}

object AsymmetricTryableDeferred {

  def concurrent[F[_] : Concurrent, A]: F[AsymmetricTryableDeferred[F, A]] = concurrentIn[F, F, A]

  def concurrentIn[F[_] : Concurrent, G[_] : Sync, A]: G[AsymmetricTryableDeferred[F, A]] =
    Sync[G].delay {
      new ConcurrentAsymmetricDeferred(new AtomicReference(State.Unset(LinkedMap.empty)))
    }

  /*
   * Implementation adopted from [cats-effect](https://typelevel.org/cats-effect)
   *
   * Copyright (c) 2017-2019 The Typelevel Cats-effect Project Developers
   *
   * Licensed under the Apache License, Version 2.0 (the "License");
   * you may not use this file except in compliance with the License.
   * You may obtain a copy of the License at
   *
   *    http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
   */

  private[this] class LinkedMap[K, +V](val entries: Map[K, (V, Long)],
                                       private[this] val insertionOrder: LongMap[K],
                                       private[this] val nextId: Long) {
    /** Returns a new map with the supplied key/value added. */
    def updated[V2 >: V](k: K, v: V2): LinkedMap[K, V2] = {
      val insertionOrderOldRemoved = entries.get(k).fold(insertionOrder) { case (_, id) => insertionOrder - id }
      new LinkedMap(entries.updated(k, (v, nextId)), insertionOrderOldRemoved.updated(nextId, k), nextId + 1)
    }

    /** Removes the element at the specified key. */
    def -(k: K): LinkedMap[K, V] =
      new LinkedMap(entries - k,
        entries
          .get(k)
          .map { case (_, id) => insertionOrder - id }
          .getOrElse(insertionOrder),
        nextId)

    /** The keys in this map, in the order they were added. */
    def keys: Iterable[K] = insertionOrder.values

    /** The values in this map, in the order they were added. */
    def values: Iterable[V] = keys.flatMap(k => entries.get(k).toList.map(_._1))

    override def toString: String =
      keys.zip(values).mkString("LinkedMap(", ", ", ")")
  }

  private[this] object LinkedMap {
    def empty[K, V]: LinkedMap[K, V] =
      emptyRef.asInstanceOf[LinkedMap[K, V]]

    private val emptyRef =
      new LinkedMap[Nothing, Nothing](Map.empty, LongMap.empty, 0)
  }

  final private[this] class Id

  sealed abstract private[this] class State[A]

  private object State {

    final case class Set[A](a: A) extends State[A]

    final case class Unset[A](waiting: LinkedMap[Id, A => Unit]) extends State[A]

  }

  final private class ConcurrentAsymmetricDeferred[F[_], A](ref: AtomicReference[State[A]])(implicit F: Concurrent[F])
    extends AsymmetricTryableDeferred[F, A] {
    def get: F[A] =
      F.suspend {
        ref.get match {
          case State.Set(a) =>
            F.pure(a)
          case State.Unset(_) =>
            F.cancelable[A] { cb =>
              val id = unsafeRegister(cb)

              @tailrec
              def unregister(): Unit =
                ref.get match {
                  case State.Set(_) => ()
                  case s@State.Unset(waiting) =>
                    val updated = State.Unset(waiting - id)
                    if (ref.compareAndSet(s, updated)) ()
                    else unregister()
                }

              F.delay(unregister())
            }
        }
      }

    def tryGet[G[_] : Sync]: G[Option[A]] =
      Sync[G].delay {
        ref.get match {
          case State.Set(a) => Some(a)
          case State.Unset(_) => None
        }
      }

    private[this] def unsafeRegister(cb: Either[Throwable, A] => Unit): Id = {
      val id = new Id

      @tailrec
      def register(): Option[A] =
        ref.get match {
          case State.Set(a) => Some(a)
          case s@State.Unset(waiting) =>
            val updated = State.Unset(waiting.updated(id, (a: A) => cb(Right(a))))
            if (ref.compareAndSet(s, updated)) None
            else register()
        }

      register().foreach(a => cb(Right(a)))
      id
    }

    def complete(a: A): F[Unit] =
      F.suspend(unsafeComplete(a))

    @tailrec
    private def unsafeComplete(a: A): F[Unit] =
      ref.get match {
        case State.Set(_) =>
          throw new IllegalStateException("Attempting to complete a Deferred that has already been completed")

        case s@State.Unset(_) =>
          if (ref.compareAndSet(s, State.Set(a))) {
            val list = s.waiting.values
            if (list.nonEmpty)
              notifyReadersLoop(a, list)
            else
              F.unit
          } else {
            unsafeComplete(a)
          }
      }

    private def notifyReadersLoop(a: A, r: Iterable[A => Unit]): F[Unit] = {
      var acc = F.unit
      val cursor = r.iterator
      while (cursor.hasNext) {
        val next = cursor.next()
        val task = F.map(F.start(F.delay(next(a))))(mapUnit)
        acc = F.flatMap(acc)(_ => task)
      }
      acc
    }

    private[this] val mapUnit = (_: Any) => ()
  }

}
