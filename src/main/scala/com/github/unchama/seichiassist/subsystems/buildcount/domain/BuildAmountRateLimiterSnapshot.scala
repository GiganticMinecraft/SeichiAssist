package com.github.unchama.seichiassist.subsystems.buildcount.domain

import cats.Functor
import cats.implicits._
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount
import io.chrisdavenport.cats.effect.time.JavaTime

import java.time.{LocalDateTime, ZoneId}

case class BuildAmountRateLimiterSnapshot(raw: BuildExpAmount, recordTime: LocalDateTime)

object BuildAmountRateLimiterSnapshot {
  def now[F[_]: JavaTime: Functor](buildExpAmount: BuildExpAmount): F[BuildAmountRateLimiterSnapshot] = {
    JavaTime[F].getLocalDateTime(ZoneId.systemDefault()).map(ldt => BuildAmountRateLimiterSnapshot(buildExpAmount, ldt))
  }
}
