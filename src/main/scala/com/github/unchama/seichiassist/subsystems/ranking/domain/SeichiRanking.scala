package com.github.unchama.seichiassist.subsystems.ranking.domain

import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount

class SeichiRanking(records: Vector[SeichiRankingRecord]) {

  import cats.implicits._

  private val sortedRecords: Vector[SeichiRankingRecord] = records.sortBy(_.seichiAmountData.expAmount).reverse

  val recordCount: Int = records.size

  val totalBreakAmount: SeichiExpAmount =
    records
      .map(_.seichiAmountData.expAmount)
      .fold(SeichiExpAmount.zero)(_.add(_))

  val recordsWithPositions: Vector[(Int, SeichiRankingRecord)] =
    sortedRecords.zipWithIndex.map { case (record, i) => (i + 1, record) }

  def positionAndRecordOf(playerName: String): Option[(Int, SeichiRankingRecord)] =
    recordsWithPositions
      .find { case (_, record) => record.playerName == playerName }

  def positionOf(playerName: String): Option[Int] =
    positionAndRecordOf(playerName).map(_._1)
}
