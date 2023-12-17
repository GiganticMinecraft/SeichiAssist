package com.github.unchama.seichiassist.subsystems.ranking.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData
import com.github.unchama.seichiassist.subsystems.ranking.domain.{
  RankingRecord,
  RankingRecordPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcBuildRankingRecordPersistence[F[_]: Sync]
    extends RankingRecordPersistence[F, BuildAmountData] {

  override def getAllRankingRecords: F[Vector[RankingRecord[BuildAmountData]]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"SELECT name, uuid, build_count from playerdata"
        .map { rs =>
          RankingRecord(
            rs.string("name"),
            UUID.fromString(rs.string("uuid")),
            BuildAmountData(BuildExpAmount(BigDecimal(rs.string("build_count"))))
          )
        }
        .list()
        .apply()
        .toVector
    }
  }
}
