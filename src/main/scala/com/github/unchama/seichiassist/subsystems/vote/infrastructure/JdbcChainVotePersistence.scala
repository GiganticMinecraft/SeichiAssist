package com.github.unchama.seichiassist.subsystems.vote.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.vote.domain.{
  ChainVoteDayNumber,
  ChainVotePersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcChainVotePersistence[F[_]: Sync] extends ChainVotePersistence[F] {
  // NOTE: 連続投票許容幅を変更する場合はここを変更してください。
  private val chainVoteAllowableWidth = 4

  override def updateChainVote(uuid: UUID): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      /*
        NOTE: 最終投票日時より(連続投票許容幅 - 1)した日時よりも
          小さかった場合に連続投票を0に戻します。
       */
      sql"""UPDATE playerdata SET chainvote = 
           | CASE WHEN DATEDIFF(last_vote, NOW()) <= ${-chainVoteAllowableWidth - 1} 
           | THEN 0 
           | ELSE chainvote + 1 
           | END""".stripMargin.execute().apply()
    }
  }

  override def getChainVoteDays(uuid: UUID): F[ChainVoteDayNumber] = {}
}
