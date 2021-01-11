package com.github.unchama.generic.effect.concurrent

import cats.data.State
import cats.effect.concurrent.{Deferred, Ref}
import cats.effect.{ConcurrentEffect, IO, Sync}
import com.github.unchama.generic.ContextCoercion
import fs2.Stream
import fs2.concurrent.Signal

/**
 * 更新が [[Signal]] により読め出せるような可変参照セル。
 *
 * [[fs2.concurrent.SignallingRef]] とほぼ同等だが、
 * 可変参照セルへの変更を行うコンテキストと更新を読みだすコンテキストが区別されている。
 * したがって、このtraitは [[fs2.concurrent.SignallingRef]] を一般化している。
 */
abstract class AsymmetricSignallingRef[G[_], F[_], A] extends Ref[G, A] {

  /**
   * この参照セルの更新を通知する [[Signal]]。
   * [[Signal.get]] と [[Ref.get]] を衝突させないために、mixinではなくcomposeしている。
   */
  val signal: Signal[F, A]

}

object AsymmetricSignallingRef {

  /**
   * Most of the implementation here is adopted from [[fs2.concurrent.SignallingRef]].
   *
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

  private final class Token extends Serializable {
    override def toString: String = s"Token(${hashCode.toHexString})"
  }

  import cats.effect.implicits._
  import cats.implicits._

  /**
   * Builds a `SignallingRef` for a `Concurrent` datatype, initialized
   * to a supplied value.
   */
  def apply[
    G[_] : Sync,
    F[_] : ConcurrentEffect : ContextCoercion[G, *[_]],
    A
  ](initial: A): G[AsymmetricSignallingRef[G, F, A]] = in[G, G, F, A](initial)

  /** Builds a `SignallingRef` for `Concurrent` datatype.
   * Like [[apply]], but initializes state using another effect constructor.
   */
  def in[
    H[_] : Sync,
    G[_] : Sync,
    F[_] : ConcurrentEffect : ContextCoercion[G, *[_]],
    A
  ](initial: A): H[AsymmetricSignallingRef[G, F, A]] =
    Ref
      .in[H, G, (A, Long, Map[Token, Deferred[F, (A, Long)]])]((initial, 0L, Map.empty))
      .map(state => new AsymmetricSignallingRefImpl[G, F, A](state))

  private final class AsymmetricSignallingRefImpl[
    G[_],
    F[_],
    A
  ](state: Ref[G, (A, Long, Map[Token, Deferred[F, (A, Long)]])])
   (implicit G: Sync[G], F: ConcurrentEffect[F], GToF: ContextCoercion[G, F])
    extends AsymmetricSignallingRef[G, F, A] {

    private type InternalState = (A, Long, Map[Token, Deferred[F, (A, Long)]])

    override val signal: Signal[F, A] = new Signal[F, A] {
      override def discrete: Stream[F, A] = {
        def go(id: Token, lastUpdate: Long): Stream[F, A] = {
          def getNext: F[(A, Long)] =
            Deferred[F, (A, Long)]
              .flatMap { deferred =>
                GToF {
                  state.modify { case s@(a, updates, listeners) =>
                    if (updates != lastUpdate) s -> (a -> updates).pure[F]
                    else (a, updates, listeners + (id -> deferred)) -> deferred.get
                  }
                }.flatten
              }

          Stream.eval(getNext).flatMap { case (a, l) => Stream.emit(a) ++ go(id, l) }
        }

        def cleanup(id: Token): F[Unit] = GToF {
          state.update(s => s.copy(_3 = s._3 - id))
        }

        Stream.bracket(F.delay(new Token))(cleanup).flatMap { id =>
          Stream.eval(GToF(state.get)).flatMap { case (a, l, _) =>
            Stream.emit(a) ++ go(id, l)
          }
        }
      }

      override def continuous: Stream[F, A] = Stream.repeatEval(get)

      override def get: F[A] = GToF(AsymmetricSignallingRefImpl.this.get)
    }

    override def get: G[A] = state.get.map(_._1)

    override def set(a: A): G[Unit] = update(_ => a)

    override def access: G[(A, A => G[Boolean])] =
      state.access.flatMap { case (snapshot, set) =>
        G.delay {
          val hasBeenCalled = new java.util.concurrent.atomic.AtomicBoolean(false)
          val setter =
            (a: A) =>
              G.delay(hasBeenCalled.compareAndSet(false, true))
                .ifM(
                  if (a == snapshot._1) set((a, snapshot._2, snapshot._3)) else G.pure(false),
                  G.pure(false)
                )
          (snapshot._1, setter)
        }
      }

    override def tryUpdate(f: A => A): G[Boolean] =
      G.map(tryModify(a => (f(a), ())))(_.isDefined)

    /**
     * 状態更新関数 `A => (A, B)` を基に、
     * 内部状態の置き換え `InternalState => InternalState` と
     * それに伴って必要である更新作用 `InternalState => G[B]` の積を作成する。
     */
    private def modification[B](f: A => (A, B)): InternalState => (InternalState, G[B]) = {
      case (a, updates, listeners) =>
        val (newA, result) = f(a)
        val newUpdates = updates + 1
        val newState = (newA, newUpdates, Map.empty[Token, Deferred[F, (A, Long)]])
        val action = listeners.toVector
          .traverse { case (_, deferred) => F.start(deferred.complete(newA -> newUpdates)) }
          .runAsync(_ => IO.unit)
          .runSync[G]
        newState -> action.as(result)
    }

    override def tryModify[B](f: A => (A, B)): G[Option[B]] =
      state.tryModify(modification(f)).flatMap {
        case None => G.pure(None)
        case Some(action) => G.map(action)(Some(_))
      }

    override def modify[B](f: A => (A, B)): G[B] = state.modify(modification(f)).flatten

    override def update(f: A => A): G[Unit] =
      modify(a => (f(a), ()))

    override def tryModifyState[B](state: State[A, B]): G[Option[B]] = {
      val f = state.runF.value
      tryModify(a => f(a).value)
    }

    override def modifyState[B](state: State[A, B]): G[B] = {
      val f = state.runF.value
      modify(a => f(a).value)
    }
  }

}
