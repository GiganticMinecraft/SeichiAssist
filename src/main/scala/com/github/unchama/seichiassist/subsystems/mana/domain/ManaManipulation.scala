package com.github.unchama.seichiassist.subsystems.mana.domain

import cats.FlatMap
import cats.effect.concurrent.Ref

/**
 * プレーヤーが持つマナに対する操作を提供するオブジェクト。
 */
trait ManaManipulation[F[_]] {

  /**
   * マナを `amount` だけ回復させる作用
   */
  def restoreAbsolute(amount: ManaAmount): F[Unit]

  /**
   * マナをマナ上限の `fraction` 倍だけ回復させる作用
   */
  def restoreFraction(fraction: Double): F[Unit]

  /**
   * マナを完全回復させる作用
   */
  def restoreCompletely: F[Unit] = restoreFraction(1)

  /**
   * マナが足りない場合はマナを消費をせずに `None` を返し、 足りていた場合は消費して `Some(amount)` を返すような作用。
   */
  def tryAcquire(amount: ManaAmount): F[Option[ManaAmount]]

}

object ManaManipulation {

  import cats.implicits._

  def fromLevelCappedAmountRef[F[_]: FlatMap](
    multiplierRef: Ref[F, ManaMultiplier]
  )(ref: Ref[F, LevelCappedManaAmount]): ManaManipulation[F] =
    new ManaManipulation[F] {
      override def restoreAbsolute(amount: ManaAmount): F[Unit] =
        ref.update(_.add(amount))

      override def restoreFraction(fraction: Double): F[Unit] =
        ref.update(cappedAmount => cappedAmount.add(cappedAmount.cap.multiply(fraction)))

      override def tryAcquire(amount: ManaAmount): F[Option[ManaAmount]] = {
        multiplierRef.get.flatMap { multiplier =>
          ref.modify { original =>
            original.tryUse(amount)(multiplier) match {
              case Some(reduced) => (reduced, Some(amount))
              case None          => (original, None)
            }
          }
        }
      }
    }

}
