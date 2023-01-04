package com.github.unchama.seichiassist.subsystems.ranking.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.ranking.domain.values.LoginTime
import com.github.unchama.seichiassist.subsystems.ranking.domain.{
  RankingRecord,
  RankingRecordPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

class JdbcLoginRankingRecordPersistence[F[_]: Sync]
    extends RankingRecordPersistence[F, LoginTime] {
  override def getAllRankingRecords: F[Vector[RankingRecord[LoginTime]]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"SELECT name,playtick from playerdata"
        .map { rs => RankingRecord(rs.string("name"), LoginTime(rs.long("playtick"))) }
        .list()
        .apply()
        .toVector
    }
  }
}
