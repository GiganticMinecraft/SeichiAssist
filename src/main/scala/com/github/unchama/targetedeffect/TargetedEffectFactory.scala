package com.github.unchama.targetedeffect

import cats.FlatMap
import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.targetedeffect

trait TargetedEffectFactory {
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
   * `f`により実行対象の[T]から[TargetedEffect]を純粋に計算して、それをすぐに実行するような作用を作成する.
   */
  def computedEffect[F[_], T, R](f: T => Kleisli[F, T, R]): Kleisli[F, T, R] =
    Kleisli(t => f(t)(t))

  import targetedeffect.syntax._
  def sequentialEffect[T](effects: TargetedEffect[T]*): TargetedEffect[T] = effects.toList.asSequentialEffect()
}
