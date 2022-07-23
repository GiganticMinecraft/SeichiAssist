package com.github.unchama.seichiassist.subsystems.vote.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.vote.domain._
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcVotePersistence[F[_]: Sync] extends VotePersistence[F] {

  // NOTE: 連続投票許容幅を変更する場合はここを変更してください。
  private val chainVoteAllowableWidth = 4

  /**
   * 投票回数をインクリメントする作用
   */
  override def voteCounterIncrement(playerName: PlayerName): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"UPDATE playerdata SET p_vote = p_vote + 1 WHERE name = ${playerName.name}"
        .execute()
        .apply()
    }
  }

  /**
   * 投票回数を返す作用
   */
  override def voteCounter(uuid: UUID): F[VoteCounter] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val votePoint = sql"SELECT p_vote FROM playerdata WHERE uuid = ${uuid.toString}"
        .map(_.int("p_vote"))
        .single()
        .apply()
        .get
      VoteCounter(votePoint)
    }
  }

  /**
   * 連続投票回数を更新する作用
   */
  override def updateChainVote(playerName: PlayerName): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      /*
        NOTE: 最終投票日時より(連続投票許容幅 - 1)した日時よりも
          小さかった場合に連続投票を0に戻します。
       */
      sql"""UPDATE playerdata SET chainvote = 
           | CASE WHEN DATEDIFF(last_vote, NOW()) <= ${-chainVoteAllowableWidth - 1}
           | THEN 0 
           | ELSE chainvote + 1 
           | END
           | WHERE name = ${playerName.name}""".stripMargin.execute().apply()
    }
  }

  /**
   * 連続投票日数を返す作用
   */
  override def chainVoteDays(uuid: UUID): F[ChainVoteDayNumber] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val chainVoteDays = sql"SELECT chainvote FROM playerdata WHERE uuid = ${uuid.toString}"
        .map(_.int("chainvote"))
        .single()
        .apply()
        .get
      ChainVoteDayNumber(chainVoteDays)
    }
  }

  /**
   * effectPointを10増加させる作用
   */
  override def increaseEffectPointsByTen(uuid: UUID): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"UPDATE playerdata SET effectpoint = 10 WHERE uuid = ${uuid.toString}"
        .execute()
        .apply()
    }
  }

  /**
   * effectPointを返す作用
   */
  override def effectPoints(uuid: UUID): F[EffectPoint] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val effectPoints = sql"SELECT effectpoint FROM playerdata WHERE name = ${uuid.toString}"
        .map(_.int("effectpoint"))
        .single()
        .apply()
        .get
      EffectPoint(effectPoints)
    }
  }

  /**
   * 投票特典を受け取った回数を増加させる作用
   */
  override def increaseVoteBenefits(uuid: UUID, benefit: VoteBenefit): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"UPDATE playerdata SET p_givenvote = p_givenvote + ${benefit.value} WHERE uuid = ${uuid.toString}"
        .execute()
        .apply()
    }
  }

  /**
   * 投票特典を受け取った回数を返す作用
   */
  override def receivedVoteBenefits(uuid: UUID): F[VoteBenefit] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val benefits = sql"SELECT p_givenvote FROM playerdata WHERE uuid = ${uuid.toString}"
        .map(_.int("p_givenvote"))
        .single()
        .apply()
        .get
      VoteBenefit(benefits)
    }
  }
}
