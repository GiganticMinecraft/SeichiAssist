package com.github.unchama.seichiassist.subsystems.ranking.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.ranking.domain.{GenericRankingRecordPersistence, RankingRecord, VoteCount}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

class JdbcVoteRankingRecordPersistence[F[_] : Sync] extends GenericRankingRecordPersistence[F, VoteCount] {
  override def getAllRankingRecords: F[Vector[RankingRecord[VoteCount]]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"SELECT name,p_vote from playerdata"
        .map { rs =>
          RankingRecord(
            rs.string("name"),
            VoteCount(rs.int("p_vote"))
          )
        }
        .list().apply().toVector
    }
  }
}
