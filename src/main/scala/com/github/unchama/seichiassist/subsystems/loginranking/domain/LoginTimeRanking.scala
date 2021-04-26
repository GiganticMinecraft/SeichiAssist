package com.github.unchama.seichiassist.subsystems.loginranking.domain

import cats.kernel.Monoid

class LoginTimeRanking(records: Vector[LoginTimeRankingRecord]) {
  private val sortedRecords = records.sortBy(_.time.inTick).reverse

  val recordCount: Int = records.size

  // 仮に一人あたり30000時間のログインが3万人あったとしても、6.48×10^13 (tick) にしかならない。これは
  // Longの最大値である9.22×10^18には程遠いので、しばらくの間はLongで事足りることが予想される。
  val totalLoginTime: LoginTime = records
    .map(_.time)
    .fold(implicitly[Monoid[LoginTime]].empty)((a, b) => implicitly[Monoid[LoginTime]].combine(a, b))

  val recordsWithPositions: Vector[(Int, LoginTimeRankingRecord)] =
    sortedRecords.zipWithIndex.map { case (record, i) => (i + 1, record) }

  def positionAndRecordOf(playerName: String): Option[(Int, LoginTimeRankingRecord)] =
    recordsWithPositions
      .find { case (_, record) => record.playerName == playerName }

  def positionOf(playerName: String): Option[Int] =
    positionAndRecordOf(playerName).map(_._1)
}
