package com.github.unchama.seichiassist.subsystems.vote

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, SyncEffect}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.vote.application.actions.ReceiveVoteBenefits
import com.github.unchama.seichiassist.subsystems.vote.bukkit.actions.BukkitReceiveVoteBenefits
import com.github.unchama.seichiassist.subsystems.vote.bukkit.command.VoteCommand
import com.github.unchama.seichiassist.subsystems.vote.bukkit.listeners.PlayerDataCreator
import com.github.unchama.seichiassist.subsystems.vote.domain._
import com.github.unchama.seichiassist.subsystems.vote.infrastructure.JdbcVotePersistence
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener

import java.util.UUID

trait System[F[_], Player] extends Subsystem[F] {
  val api: VoteAPI[F, Player]
}

object System {

  def wired[F[_]: ConcurrentEffect: OnMinecraftServerThread, G[_]: SyncEffect](
    implicit breakCountAPI: BreakCountAPI[F, G, Player]
  ): System[F, Player] = {
    implicit val _votePersistence: VotePersistence[F] = new JdbcVotePersistence[F]
    val _receiveVoteBenefits: ReceiveVoteBenefits[F, Player] =
      new BukkitReceiveVoteBenefits[F, G]()

    new System[F, Player] {
      override implicit val api: VoteAPI[F, Player] = new VoteAPI[F, Player] {
        override def count(uuid: UUID): F[VoteCount] =
          _votePersistence.currentVoteCount(uuid)

        override def currentConsecutiveVoteStreakDays(uuid: UUID): F[ChainVoteDayNumber] =
          _votePersistence.currentConsecutiveVoteStreakDay(uuid)

        override def decreaseEffectPoint(uuid: UUID, effectPoint: EffectPoint): F[Unit] =
          _votePersistence.decreaseEffectPoints(uuid, effectPoint)

        override def effectPoints(player: Player): F[EffectPoint] =
          _votePersistence.effectPoints(player.getUniqueId)

        override def receivedVoteBenefits(uuid: UUID): F[VoteCountForReceive] =
          _votePersistence.receivedVoteBenefits(uuid)

        override def receiveVoteBenefits: Kleisli[F, Player, Unit] = Kleisli { player =>
          _receiveVoteBenefits.receive(player)
        }
      }

      override val commands: Map[String, TabExecutor] = Map(
        "vote" -> new VoteCommand[F].executor
      )

      override val listeners: Seq[Listener] = Seq(new PlayerDataCreator[F])
    }
  }

}
