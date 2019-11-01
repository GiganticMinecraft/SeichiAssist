package com.github.unchama.targetedeffect

import cats.data.Kleisli
import cats.effect.IO
import cats.{Apply, Monoid}

object TargetedEffectInstances {
  /**
   * Minecraft内の何らかの対象[T]に向けた作用を持ち,
   * [runFor]メソッドにより作用を[T]に関して及ぼすことができるオブジェクトのtrait.
   */
  implicit def monoid[T]: Monoid[TargetedEffect[T]] = new Monoid[TargetedEffect[T]] {
    override def empty(): TargetedEffect[T] = TargetedEffects.emptyEffect

    override def combine(a: TargetedEffect[T], b: TargetedEffect[T]): TargetedEffect[T] = {
      import TargetedEffects.KleisliCombine

      a.followedBy(b)
    }
  }
}

object TargetedEffects {
  /**
   * 何も作用を及ぼさないような[TargetedEffect].
   */
  val emptyEffect: TargetedEffect[Any] = Kleisli.pure(())

  implicit class KleisliCombine[F[_]: Apply, A, B](val effect: Kleisli[F, A, B]) {
    def followedBy[AA <: A](anotherEffect: Kleisli[F, AA, B]): Kleisli[F, AA, B] = {
      import cats.implicits._
      Kleisli(aa => effect(aa) *> anotherEffect(aa))
    }
  }

  implicit class TargetedEffectFold[T](val effects: List[TargetedEffect[T]]) {
    def asSequentialEffect(): TargetedEffect[T] = TargetedEffectInstances.monoid[T].combineAll(effects)
  }

  /**
   * 同期的な副作用`f`を`TargetedEffect`内に持ち回すようにする.
   */
  def delay[T](f: T => Unit): TargetedEffect[T] = Kleisli(t => IO.delay(f(t)))

  /**
   * [TargetedEffect]を非純粋に計算しそれをすぐに実行するような作用を作成する.
   */
  def deferredEffect[T](f: IO[TargetedEffect[T]]): TargetedEffect[T] = Kleisli(t => f.flatMap(_ (t)))

  /**
   * 実行対象の[T]から[TargetedEffect]を非純粋に計算しそれをすぐに実行するような作用を作成する.
   */
  def computedEffect[T](f: T => TargetedEffect[T]): TargetedEffect[T] = Kleisli(t => f(t)(t))

  def sequentialEffect[T](effects: TargetedEffect[T]*): TargetedEffect[T] = effects.toList.asSequentialEffect()
}