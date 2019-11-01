package com.github.unchama

import cats.data.Kleisli
import cats.effect.IO

package object targetedeffect {
  type TargetedEffect[-T] = Kleisli[IO, T, Unit]

  /**
   * 何も作用を及ぼさないような[TargetedEffect].
   */
  val emptyEffect: TargetedEffect[Any] = Kleisli.pure(())

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

  import targetedeffect.syntax._
  def sequentialEffect[T](effects: TargetedEffect[T]*): TargetedEffect[T] = effects.toList.asSequentialEffect()
}
