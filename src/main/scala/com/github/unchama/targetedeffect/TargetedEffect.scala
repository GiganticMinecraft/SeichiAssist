package com.github.unchama.targetedeffect

import cats.FlatMap
import cats.data.Kleisli
import cats.effect.{IO, Timer}
import cats.kernel.Monoid

import scala.concurrent.duration.FiniteDuration

object TargetedEffect {
  /**
   * 何も作用を及ぼさないような[TargetedEffect].
   */
  val emptyEffect: TargetedEffect[Any] = Kleisli.pure(())

  /**
   * 同期的な副作用`f`を`TargetedEffect`内に持ち回すようにする.
   */
  def delay[T](f: T => Unit): TargetedEffect[T] = Kleisli(t => IO.delay(f(t)))
}

object DeferredEffect {
  /**
   * `F`計算の結果の作用を`F`内で実行するような計算を返す.
   *
   * 返される`Kleisli`は、環境`t`を受け取り、`f`から結果`r: Kleisli[F, T, R])`を取り出し、
   * それぞれを`r(t)`に`fmap`する、といった動作をする計算となる.
   */
  def apply[F[_]: FlatMap, T, R](f: F[Kleisli[F, T, R]]): Kleisli[F, T, R] = {
    import cats.implicits._

    Kleisli(t =>
      for {
        r <- f
        rr <- r(t)
      } yield rr
    )
  }
}

object SequentialEffect {
  def apply[T](effects: TargetedEffect[T]*): TargetedEffect[T] =
    SequentialEffect(effects.toList)

  def apply[T](effects: List[TargetedEffect[T]]): TargetedEffect[T] = {
    import cats.implicits._

    Monoid[TargetedEffect[T]].combineAll(effects)
  }
}

object ComputedEffect {
  /**
   * `f`により実行対象の[T]から[TargetedEffect]を純粋に計算して、それをすぐに実行するような作用を作成する.
   */
  def apply[F[_], T, R](f: T => Kleisli[F, T, R]): Kleisli[F, T, R] = Kleisli(t => f(t)(t))
}

object UnfocusedEffect {
  def apply(effect: => Unit): TargetedEffect[Any] = TargetedEffect.delay(_ => effect)
}

object DelayEffect {
  def apply(duration: FiniteDuration)(implicit timer: Timer[IO]): TargetedEffect[Any] = Kleisli.liftF(IO.sleep(duration))
}
