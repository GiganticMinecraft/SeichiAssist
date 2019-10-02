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

  implicit def monoid[T]: Monoid[TargetedEffect[T]] = new Monoid[TargetedEffect[T]] {
    override def empty(): TargetedEffect[T] = EmptyEffect

    override def combine(a: TargetedEffect[T], b: TargetedEffect[T]): TargetedEffect[T] = {
      import TargetedEffects.TargetedEffectCombine

      a.followedBy(b)
    }
  }

  def apply[T](effect: T => Unit): TargetedEffect[T] = (minecraftObject: T) => IO { effect(minecraftObject) }
}

object TargetedEffects {
  implicit class TargetedEffectCombine[T](val effect: TargetedEffect[T]) {
    def followedBy[T1 <: T](anotherEffect: TargetedEffect[T1]): TargetedEffect[T1] =
      t1 => for {
        _ <- effect(t1)
        _ <- anotherEffect(t1)
      } yield ()
  }

  implicit class TargetedEffectFold[T](val effects: List[TargetedEffect[T]]) {
    def asSequentialEffect(): TargetedEffect[T] = TargetedEffect.monoid[T].combineAll(effects)
  }

  /**
   * [TargetedEffect]を非純粋に計算しそれをすぐに実行するような作用を作成する.
   */
  def deferredEffect[T](f: IO[TargetedEffect[T]]): TargetedEffect[T] = t => f.flatMap(_(t))

  /**
  * 実行対象の[T]から[TargetedEffect]を非純粋に計算しそれをすぐに実行するような作用を作成する.
  */
  def computedEffect[T](f: T => TargetedEffect[T]): TargetedEffect[T] = t => f(t)(t)

  def sequentialEffect[T](effects: TargetedEffect[T]*): TargetedEffect[T] = effects.toList.asSequentialEffect()
}