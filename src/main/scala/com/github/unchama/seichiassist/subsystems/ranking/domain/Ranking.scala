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

  /**
   * ランキングのレコードと、そのレコードの順位の組の集まり。
   *
   * この順位はタイを考慮する。
   * つまり、二つのレコード `r1` と `r2` があり、 `r1.value` と `r2.value` が[[Order]]により等しければ、
   * `r1` と `r2` の順位は同じになる。
   */
  val recordsWithPositions: Vector[(RankingRecord[R], Int)] = Vector.from {
    var positionOfPreviousRecord = 0

    for {
      index <- sortedRecords.indices
    } yield {
      // より小さな値に出くわしたら、記録する順位をindex + 1に戻す
      if (index == 0 || (sortedRecords(index).value < sortedRecords(index - 1).value)) {
        positionOfPreviousRecord = index + 1
      }

      (sortedRecords(index), positionOfPreviousRecord)
    }
  }

  def positionAndRecordOf(playerName: String): Option[(RankingRecord[R], Int)] =
    recordsWithPositions
      .find { case (record, _) => record.playerName == playerName }

  def positionOf(playerName: String): Option[Int] =
    positionAndRecordOf(playerName).map(_._2)
}
