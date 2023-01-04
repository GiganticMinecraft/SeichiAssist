package com.github.unchama.seichiassist.subsystems.ranking.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.ranking.domain.values.VoteCount
import com.github.unchama.seichiassist.subsystems.ranking.domain.{
  RankingRecord,
  RankingRecordPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

class JdbcVoteRankingRecordPersistence[F[_]: Sync]
    extends RankingRecordPersistence[F, VoteCount] {
  override def getAllRankingRecords: F[Vector[RankingRecord[VoteCount]]] = Sync[F].delay {
    DB.readOnly { using session =>
      sql"SELECT name,p_vote from playerdata"
        .map { rs => RankingRecord(rs.string("name"), VoteCount(rs.int("p_vote"))) }
        .list()
        .apply()
        .toVector
    }
  }
}
