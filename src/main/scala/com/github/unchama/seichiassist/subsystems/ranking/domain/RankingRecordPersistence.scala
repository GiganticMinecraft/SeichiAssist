package com.github.unchama.seichiassist.subsystems.ranking.domain

/**
 *
 * @tparam F 作用の文脈
 * @tparam RR 単一のレコードの型
 */
trait RankingRecordPersistence[F[_], RR] {

  def getAllRankingRecords: F[Vector[RR]]

}
