package com.github.unchama.seichiassist.subsystems.ranking.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.ranking.domain.values.LoginTime
import com.github.unchama.seichiassist.subsystems.ranking.domain.{
  RankingRecord,
  RankingRecordPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcLoginRankingRecordPersistence[F[_]: Sync]
    extends RankingRecordPersistence[F, LoginTime] {
  override def getAllRankingRecords: F[Vector[RankingRecord[LoginTime]]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"SELECT name, uuid, playtick from playerdata"
        .map { rs =>
          RankingRecord(
            rs.string("name"),
            UUID.fromString(rs.string("uuid")),
            LoginTime(rs.long("playtick"))
          )
        }
        .list()
        .apply()
        .toVector
    }
  }
}
