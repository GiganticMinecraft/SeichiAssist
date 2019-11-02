package com.github.unchama

import cats.FlatMap
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
   * `F`計算の結果の作用を`F`内で実行するような計算を返す.
   *
   * 返される`Kleisli`は、環境`t`を受け取り、`f`から結果`r: Kleisli[F, T, R])`を取り出し、
   * それぞれを`r(t)`に`fmap`する、といった動作をする計算となる.
   */
  def deferredEffect[F[_]: FlatMap, T, R](f: F[Kleisli[F, T, R]]): Kleisli[F, T, R] = {
    import cats.implicits._

    Kleisli(t =>
      for {
        r <- f
        rr <- r(t)
      } yield rr
    )
  }

  /**
   * 実行対象の[T]から[TargetedEffect]を非純粋に計算しそれをすぐに実行するような作用を作成する.
   */
  def computedEffect[T](f: T => TargetedEffect[T]): TargetedEffect[T] = Kleisli(t => f(t)(t))

  import targetedeffect.syntax._
  def sequentialEffect[T](effects: TargetedEffect[T]*): TargetedEffect[T] = effects.toList.asSequentialEffect()
}
