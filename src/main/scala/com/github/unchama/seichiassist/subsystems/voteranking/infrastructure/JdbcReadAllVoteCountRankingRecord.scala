package com.github.unchama.seichiassist.subsystems.voteranking.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.loginranking.domain.LoginTimeRankingRecord
import com.github.unchama.seichiassist.subsystems.ranking.domain.RankingRecordPersistence
import com.github.unchama.seichiassist.subsystems.voteranking.domain.VoteCountRankingRecord
import scalikejdbc._

class JdbcReadAllVoteCountRankingRecord[F[_] : Sync] extends RankingRecordPersistence[F, VoteCountRankingRecord]{
  override def getAllRankingRecords: F[Vector[VoteCountRankingRecord]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"""SELECT name,p_vote from playerdata"""
        .map { rs =>
          VoteCountRankingRecord(
            rs.string("name"),
            rs.int("p_vote")
          )
        }
        .list()
        .apply()
        .toVector
    }
  }
}
