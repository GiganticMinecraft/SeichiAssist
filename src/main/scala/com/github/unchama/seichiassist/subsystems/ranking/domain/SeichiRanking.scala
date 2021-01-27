package com.github.unchama.seichiassist.subsystems.ranking.domain

import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount

class SeichiRanking(records: Vector[SeichiRankingRecord]) {

  import cats.implicits._

  private val sortedRecords: Vector[SeichiRankingRecord] = records.sortBy(_.seichiAmountData.expAmount)

  val totalBreakAmount: SeichiExpAmount =
    records
      .map(_.seichiAmountData.expAmount)
      .fold(SeichiExpAmount.zero)(_.add(_))

  val recordsWithPositions: Vector[(Int, SeichiRankingRecord)] =
    sortedRecords.zipWithIndex.map { case (record, i) => (i + 1, record) }
}
