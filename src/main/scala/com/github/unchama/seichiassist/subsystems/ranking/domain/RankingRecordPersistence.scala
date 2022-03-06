package com.github.unchama.seichiassist.subsystems.ranking.domain

/**
 * @tparam F
 *   ランキングレコードを取得する作用の文脈
 * @tparam R
 *   レコードが記録する値の型
 */
trait RankingRecordPersistence[F[_], R] {

  def getAllRankingRecords: F[Vector[RankingRecord[R]]]

}
