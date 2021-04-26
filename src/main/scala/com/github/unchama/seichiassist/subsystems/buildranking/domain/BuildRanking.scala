package com.github.unchama.seichiassist.subsystems.buildranking.domain

class BuildRanking(records: Vector[BuildRankingRecord]) {
  private val sortedRecords = records.sortBy(_.buildCountAmount.amount).reverse

  val recordCount: Int = records.size

  val totalBuildExp: BigDecimal =
    records
      .map(_.buildCountAmount.amount)
      .sum

  val recordsWithPositions: Vector[(Int, BuildRankingRecord)] =
    sortedRecords.zipWithIndex.map { case (record, i) => (i + 1, record) }

  def positionAndRecordOf(playerName: String): Option[(Int, BuildRankingRecord)] =
    recordsWithPositions
      .find { case (_, record) => record.playerName == playerName }

  def positionOf(playerName: String): Option[Int] =
    positionAndRecordOf(playerName).map(_._1)
}
