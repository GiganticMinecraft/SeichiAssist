package com.github.unchama.seichiassist.subsystems.vote.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.vote.domain.{
  PlayerName,
  VotePoint,
  VotePointPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcVotePointPersistence[F[_]: Sync] extends VotePointPersistence[F] {
  override def increment(playerName: PlayerName): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"UPDATE playerdata SET p_vote = p_vote + 1 WHERE name = $playerName".execute().apply()
    }
  }

  override def votePoint(uuid: UUID): F[VotePoint] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val votePoint = sql"SELECT p_vote FROM playerdata WHERE uuid = ${uuid.toString}"
        .map(_.int("p_vote"))
        .single()
        .apply()
        .get
      VotePoint(votePoint)
    }
  }
}
