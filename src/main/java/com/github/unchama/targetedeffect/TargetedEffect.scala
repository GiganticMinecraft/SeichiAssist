package com.github.unchama.targetedeffect

import cats.Monoid
import cats.effect.IO
import com.github.unchama.targetedeffect.TargetedEffect.TargetedEffect

object TargetedEffect {
  /**
   * Minecraft内の何らかの対象[T]に向けた作用を持ち,
   * [runFor]メソッドにより作用を[T]に関して及ぼすことができるオブジェクトのtrait.
   */
  // TODO move type alias to package object
  type TargetedEffect[-T] = T => IO[Unit]

  def monoid[T]: Monoid[TargetedEffect[T]] = new Monoid[TargetedEffect[T]] {
    override def empty(): TargetedEffect[T] = EmptyEffect

    override def combine(a: TargetedEffect[T], b: TargetedEffect[T]): TargetedEffect[T] =
      t => for {
        _ <- a(t)
        _ <- b(t)
      } yield Unit
  }

  def apply[T](effect: T => Unit): TargetedEffect[T] = (minecraftObject: T) => IO { effect(minecraftObject) }
}

object TargetedEffects {
  implicit class TargetedEffectCombine[T](val effect: TargetedEffect[T]) {
    def +(anotherEffect: TargetedEffect[T]): TargetedEffect[T] =
      TargetedEffect.monoid[T].combine(effect, anotherEffect)
  }

  implicit class TargetedEffectFold[T](val effects: List[TargetedEffect[T]]) {
    def asSequentialEffect(): TargetedEffect[T] = TargetedEffect.monoid[T].combineAll(effects)
  }

  /**
   * [TargetedEffect]を非純粋に計算しそれをすぐに実行するような作用を作成する.
   */
  def deferredEffect[T](f: IO[TargetedEffect[T]]): TargetedEffect[T] =
    t =>
      for {
        effect <- f
        _ <- effect(t)
      } yield Unit

  /**
  * 実行対象の[T]から[TargetedEffect]を非純粋に計算しそれをすぐに実行するような作用を作成する.
  */
  def computedEffect[T](f: T => TargetedEffect[T]): TargetedEffect[T] =
    t => for { _ <- f(t)(t) } yield Unit

  def sequentialEffect[T](effects: TargetedEffect[T]*): TargetedEffect[T] = effects.toList.asSequentialEffect()
}