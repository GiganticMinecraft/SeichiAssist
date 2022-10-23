package com.github.unchama.seichiassist.subsystems.vote

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
    implicit val votePersistence: VotePersistence[F] = new JdbcVotePersistence[F]
    val receiveVoteBenefits: ReceiveVoteBenefits[F, Player] =
      new BukkitReceiveVoteBenefits[F, G]()

    new System[F, Player] {
      override implicit val api: VoteAPI[F, Player] = new VoteAPI[F, Player] {
        override def voteCounter(uuid: UUID): F[VoteCounter] =
          votePersistence.voteCounter(uuid)

        override def chainVoteDayNumber(uuid: UUID): F[ChainVoteDayNumber] =
          votePersistence.chainVoteDays(uuid)

        override def decreaseEffectPoint(uuid: UUID, effectPoint: EffectPoint): F[Unit] =
          votePersistence.decreaseEffectPoints(uuid, effectPoint)

        override def increaseEffectPointsByTen(uuid: UUID): F[Unit] =
          votePersistence.increaseEffectPointsByTen(uuid)

        override def effectPoints(player: Player): F[EffectPoint] =
          votePersistence.effectPoints(player.getUniqueId)

        override def increaseVoteBenefits(uuid: UUID, benefit: VoteBenefit): F[Unit] =
          votePersistence.increaseVoteBenefits(uuid, benefit)

        override def receivedVoteBenefits(uuid: UUID): F[VoteBenefit] =
          votePersistence.receivedVoteBenefits(uuid)

        import cats.implicits._

        override def restVoteBenefits(uuid: UUID): F[VoteBenefit] = for {
          voteCounter <- voteCounter(uuid)
          receivedVote <- receivedVoteBenefits(uuid)
        } yield VoteBenefit(voteCounter.value - receivedVote.value)

        override def receiveVotePrivilege(player: Player): F[Unit] =
          receiveVoteBenefits.receive(player)
      }

      override val commands: Map[String, TabExecutor] = Map(
        "vote" -> new VoteCommand[F].executor
      )

      override val listeners: Seq[Listener] = Seq(new PlayerDataCreator[F])
    }
  }

}
