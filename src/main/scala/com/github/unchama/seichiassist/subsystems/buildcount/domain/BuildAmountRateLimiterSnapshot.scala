package com.github.unchama.seichiassist.subsystems.buildcount.domain

import cats.Functor
import cats.effect.Clock
import cats.implicits._
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount

import java.time.{LocalDateTime, ZoneId}

/**
 * `RateLimiter[F, BuildExpAmount]`によって保持された残量について日時付きで保存するクラス
 * @param amount
 *   そのタイムスライスにおけるリクエスト量の上限
 * @param recordTime
 *   取得した時間
 */
case class BuildAmountRateLimiterSnapshot(amount: BuildExpAmount, recordTime: LocalDateTime)

object BuildAmountRateLimiterSnapshot {
  def now[F[_]: Clock: Functor](
    buildExpAmount: BuildExpAmount
  ): F[BuildAmountRateLimiterSnapshot] = {
    Clock[F]
      .realTimeInstant
      .map(_.atZone(ZoneId.systemDefault()).toLocalDateTime)
      .map(ldt => BuildAmountRateLimiterSnapshot(buildExpAmount, ldt))
  }
}
