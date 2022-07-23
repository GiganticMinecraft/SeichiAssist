package com.github.unchama.seichiassist.subsystems.vote

import cats.effect.Sync
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.vote.domain.{
  ChainVoteDayNumber,
  PlayerName,
  VotePoint
}
import com.github.unchama.seichiassist.subsystems.vote.infrastructure.{
  JdbcChainVotePersistence,
  JdbcVotePointPersistence
}

import java.util.UUID

trait System[F[_]] extends Subsystem[F] {
  val api: VoteAPI[F]
}

object System {

  def wired[F[_]: Sync: OnMinecraftServerThread]: System[F] = {
    val chainVotePersistence = new JdbcChainVotePersistence[F]
    val votePointPersistence = new JdbcVotePointPersistence[F]

    new System[F] {
      override val api: VoteAPI[F] = new VoteAPI[F] {
        override def incrementVotePoint(playerName: PlayerName): F[Unit] =
          votePointPersistence.increment(playerName)

        override def updateChainVote(playerName: PlayerName): F[Unit] =
          chainVotePersistence.updateChainVote(playerName)

        override def votePoint(uuid: UUID): F[VotePoint] =
          votePointPersistence.votePoint(uuid)

        override def chainVoteDayNumber(uuid: UUID): F[ChainVoteDayNumber] =
          chainVotePersistence.getChainVoteDays(uuid)
      }
    }
  }

}
