package com.github.unchama.seichiassist.subsystems.buildranking.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount
import com.github.unchama.seichiassist.subsystems.buildranking.domain.BuildRankingRecord
import com.github.unchama.seichiassist.subsystems.ranking.domain.RankingRecordPersistence
import scalikejdbc._

class JdbcReadAllBuildAmountData[F[_] : Sync] extends RankingRecordPersistence[F, BuildRankingRecord] {
  override def getAllRankingRecords: F[Vector[BuildRankingRecord]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"""SELECT name,build_count from playerdata"""
        .map { rs =>
          BuildRankingRecord(
            rs.string("name"),
            BuildExpAmount(BigDecimal(rs.string("build_count")))
          )
        }
        .list()
        .apply()
        .toVector

    }
  }
}
