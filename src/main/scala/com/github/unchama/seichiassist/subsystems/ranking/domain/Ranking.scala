package com.github.unchama.seichiassist.subsystems.ranking.domain

import cats.{Monoid, Order}

/**
 * ある時点でのランキング全体の情報を持つオブジェクトのクラス。
 * @tparam R
 *   各プレーヤーのレコードが持つデータ型
 */
class Ranking[R: Order: Monoid](records: Vector[RankingRecord[R]]) {
  import cats.implicits._

  private val sortedRecords: Vector[RankingRecord[R]] = records.sortBy(_.value).reverse

  val recordCount: Int = records.size

  val total: R = Monoid[R].combineAll(records.map(_.value))

  /**
   * ランキングのレコードと、そのレコードの順位の組の集まり。
   *
   * この順位はタイを考慮する。 つまり、二つのレコード `r1` と `r2` があり、 `r1.value` と `r2.value` が[[Order]]により等しければ、 `r1`
   * と `r2` の順位は同じになる。
   */
  val recordsWithPositions: Vector[RankingRecordWithPosition[R]] = Vector.from {
    var positionOfPreviousRecord = 0

    for {
      index <- sortedRecords.indices
    } yield {
      // より小さな値に出くわしたら、記録する順位をindex + 1に戻す
      if (index == 0 || (sortedRecords(index).value < sortedRecords(index - 1).value)) {
        positionOfPreviousRecord = index + 1
      }

      RankingRecordWithPosition(sortedRecords(index), positionOfPreviousRecord)
    }
  }

  /**
   * `recordsWithPositions` のインデックスで、 `playerName` のレコードが格納されたもの。 もしそのようなレコードが存在しなければ `None`
   * が返される。
   */
  private def indexOfRecordOf(playerName: String): Option[Int] = {
    val index = recordsWithPositions.indexWhere(_.record.playerName == playerName)

    if (index == -1) None else Some(index)
  }

  def positionAndRecordOf(playerName: String): Option[RankingRecordWithPosition[R]] =
    indexOfRecordOf(playerName).map(recordsWithPositions)

  def bestRecordBelow(playerName: String): Option[RankingRecordWithPosition[R]] =
    indexOfRecordOf(playerName).flatMap { recordIndex =>
      // recordsWithPositionは降順ソートされているので、該当レコードよりも奥を切り出し、
      // レコード値が異なるような一番手前のレコードを持ってくればよい
      recordsWithPositions
        .drop(recordIndex + 1)
        .find(_.record.value neqv recordsWithPositions(recordIndex).record.value)
    }

  def worstRecordAbove(playerName: String): Option[RankingRecordWithPosition[R]] =
    indexOfRecordOf(playerName).flatMap { recordIndex =>
      // recordsWithPositionは降順ソートされているので、該当レコードよりも手前を切り出し、
      // レコード値が異なるような一番奥のレコードを持ってくればよい
      recordsWithPositions
        .take(recordIndex)
        .findLast(_.record.value neqv recordsWithPositions(recordIndex).record.value)
    }

  def positionOf(playerName: String): Option[Int] =
    positionAndRecordOf(playerName).map(_.positionInRanking)
}
