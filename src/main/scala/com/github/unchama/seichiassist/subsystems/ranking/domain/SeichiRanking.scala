package com.github.unchama.seichiassist.subsystems.ranking.domain

class SeichiRanking(records: Vector[SeichiRankingRecord]) {

  import cats.implicits._

  private val sortedRecords: Vector[SeichiRankingRecord] = records.sortBy(_.seichiAmountData.expAmount)

  val recordsWithPositions: Vector[(Int, SeichiRankingRecord)] =
    sortedRecords.zipWithIndex.map { case (record, i) => (i + 1, record) }
}
