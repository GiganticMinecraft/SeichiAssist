package com.github.unchama.seichiassist.subsystems.ranking.domain

import cats.{Monoid, Order}

/**
 * ある時点でのランキング全体の情報を持つオブジェクトのクラス。
 * @tparam R 各プレーヤーのレコードが持つデータ型
 */
class Ranking[R: Order: Monoid](records: Vector[RankingRecord[R]]) {
  import cats.implicits._

  private val sortedRecords: Vector[RankingRecord[R]] = records.sortBy(_.value).reverse

  val recordCount: Int = records.size

  val total: R = Monoid[R].combineAll(records.map(_.value))

  val recordsWithPositions: Vector[(Int, RankingRecord[R])] =
    sortedRecords.zipWithIndex.map { case (record, i) => (i + 1, record) }

  def positionAndRecordOf(playerName: String): Option[(Int, RankingRecord[R])] =
    recordsWithPositions
      .find { case (_, record) => record.playerName == playerName }

  def positionOf(playerName: String): Option[Int] =
    positionAndRecordOf(playerName).map(_._1)
}
