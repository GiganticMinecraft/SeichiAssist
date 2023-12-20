package com.github.unchama.generic.effect.concurrent

import cats.Applicative
import cats.data.State
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Resource, Sync}
import com.github.unchama.fs2.workaround.fs3.Fs3Topic
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.stream.ReorderingPipe
import com.github.unchama.generic.effect.stream.ReorderingPipe.TimeStamped
import com.github.unchama.generic.{ContextCoercion, Token}
import fs2.Stream

/**
 * 更新が [[Stream]] により読め出せるような可変参照セル。
 *
 * [[fs2.concurrent.SignallingRef]] とほぼ同等だが、 可変参照セルへの変更を行うコンテキストと更新を読みだすコンテキストが区別されている。
 * したがって、このtraitは [[fs2.concurrent.SignallingRef]] を一般化している。
 */
abstract class AsymmetricSignallingRef[G[_], F[_], A] extends Ref[G, A] {

  /**
   * 更新値へのsubscriptionを [[Resource]] として表現したもの。 subscriptionが有効になった後に [[Resource]] として利用可能になり、
   * 利用が終了した後に自動的にunsubscribeされる。
   *
   * [[Resource]] として利用可能である間は更新が行われた順に更新値が全て得られることが保証される。
   */
  val valuesAwait: Resource[F, Stream[F, A]]

}

object AsymmetricSignallingRef {

  /**
   * Most of the implementation here is adopted from [[fs2.concurrent.SignallingRef]].
   *
   * Copyright (c) 2013 Functional Streams for Scala
   *
   * Permission is hereby granted, free of charge, to any person obtaining a copy of this
   * software and associated documentation files (the "Software"), to deal in the Software
   * without restriction, including without limitation the rights to use, copy, modify, merge,
   * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons
   * to whom the Software is furnished to do so, subject to the following conditions:
   *
   * The above copyright notice and this permission notice shall be included in all copies or
   * substantial portions of the Software.
   *
   * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
   * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
   * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
   * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
   * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
   * DEALINGS IN THE SOFTWARE.
   */

  import cats.implicits._

  /**
   * 指定された値で初期化された[[AsymmetricSignallingRef]]を作成する作用。
   */
  def apply[G[_]: Sync, F[_]: ConcurrentEffect: ContextCoercion[G, *[_]], A](
    initial: A
  ): G[AsymmetricSignallingRef[G, F, A]] = in[G, G, F, A](initial)

  /**
   * 指定された値で初期化された[[AsymmetricSignallingRef]]を作成する作用。
   *
   * [[apply]] とほぼ等価であるが、状態の作成を別の作用型の中で行う。
   */
  def in[H[_]: Sync, G[_]: Sync, F[_]: ConcurrentEffect: ContextCoercion[G, *[_]], A](
    initial: A
  ): H[AsymmetricSignallingRef[G, F, A]] = {
    val initialState = TimeStamped(new Token, new Token, initial)

    Applicative[H]
      .map2(Ref.in[H, G, TimeStamped[A]](initialState), Fs3Topic.in[H, F, TimeStamped[A]]) {
        case (ref, topic) =>
          new AsymmetricSignallingRefImpl[G, F, A](ref, topic)
      }
  }

  private final class AsymmetricSignallingRefImpl[G[_], F[_], A](
    state: Ref[G, TimeStamped[A]],
    changeTopic: Fs3Topic[F, TimeStamped[A]]
  )(implicit G: Sync[G], F: ConcurrentEffect[F], GToF: ContextCoercion[G, F])
      extends AsymmetricSignallingRef[G, F, A] {

    private val topicQueueSize = 10

    override val valuesAwait: Resource[F, Stream[F, A]] =
      changeTopic.subscribeAwait(topicQueueSize).flatMap { subscription =>
        Resource
          .eval {
            state.get.map { currentValue =>
              subscription
                .through(ReorderingPipe.withInitialToken[F, A](currentValue.nextStamp))
            }
          }
          .mapK(GToF)
      }

    override def get: G[A] = state.get.map(_.value)

    override def set(a: A): G[Unit] = update(_ => a)

    /**
     * 状態更新関数 `A => (A, B)` を基に、 内部状態の置き換え `InternalState[A] => InternalState[A]` と
     * それに伴って必要である通知作用 `A => G[B]` の積を作成する。
     */
    private def updateAndNotify[B](f: A => (A, B)): TimeStamped[A] => (TimeStamped[A], G[B]) = {
      case TimeStamped(_, nextStamp, a) =>
        val (newA, result) = f(a)
        val newATimeStamped = TimeStamped(nextStamp, new Token, newA)
        val action =
          EffectExtra.runAsyncAndForget[F, G, Unit](changeTopic.publish1(newATimeStamped).void)
        newATimeStamped -> action.as(result)
    }

    override def access: G[(A, A => G[Boolean])] =
      state.access.map {
        case (snapshot, set) =>
          val setter = { (newValue: A) =>
            val (newState, notify) = updateAndNotify(_ => (newValue, ()))(snapshot)
            set(newState).flatTap { succeeded => notify.whenA(succeeded) }
          }

          (snapshot.value, setter)
      }

    override def tryUpdate(f: A => A): G[Boolean] =
      G.map(tryModify(a => (f(a), ())))(_.isDefined)

    override def tryModify[B](f: A => (A, B)): G[Option[B]] =
      state.tryModify(updateAndNotify(f)).flatMap {
        case None         => G.pure(None)
        case Some(action) => G.map(action)(Some(_))
      }

    override def modify[B](f: A => (A, B)): G[B] = state.modify(updateAndNotify(f)).flatten

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
