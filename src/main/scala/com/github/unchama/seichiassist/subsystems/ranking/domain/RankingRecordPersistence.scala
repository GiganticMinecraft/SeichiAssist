package com.github.unchama.seichiassist.subsystems.ranking.domain

trait RankingRecordPersistence[F[_]] {

  def getAllRankingRecords: F[Vector[SeichiRankingRecord]]

}
