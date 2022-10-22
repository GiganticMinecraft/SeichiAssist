package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy

import cats.effect.{ConcurrentEffect, IO, SyncIO}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.datarepository.bukkit.player.{
  BukkitRepositoryControls,
  PlayerDataRepository
}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.repository.{
  FairyManaRecoveryRoutineFiberRepositoryDefinition,
  SpeechServiceRepositoryDefinitions
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.BukkitFairySpeech
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.gateway.BukkitFairySpeechGateway
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.listeners.FairyPlayerJoinGreeter
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.routines.BukkitFairyRoutine
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property._
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.resources.bukkit.FairyLoreTable
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.{
  FairyPersistence,
  FairySpeech,
  FairySpeechGateway
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.infrastructure.JdbcFairyPersistence
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.service.FairySpeechService
import org.bukkit.entity.Player
import org.bukkit.event.Listener

import java.util.UUID

trait System[F[_], G[_], Player] extends Subsystem[F] {
  val api: FairyAPI[F, G, Player]
}

object System {

  def wired(
    implicit breakCountAPI: BreakCountAPI[IO, SyncIO, Player],
    voteAPI: VoteAPI[IO, Player],
    manaApi: ManaApi[IO, SyncIO, Player],
    repeatingTaskContext: RepeatingTaskContext,
    concurrentEffect: ConcurrentEffect[IO]
  ): SyncIO[System[IO, SyncIO, Player]] = {
    implicit val persistence: FairyPersistence[IO] = new JdbcFairyPersistence[IO]
    implicit val fairySpeechGatewayProvider: Player => FairySpeechGateway[SyncIO] =
      new BukkitFairySpeechGateway[SyncIO](_)
    val fairySpeechProvider
      : PlayerDataRepository[FairySpeechService[SyncIO]] => FairySpeech[IO, Player] =
      fairySpeechService => new BukkitFairySpeech[IO, SyncIO](fairySpeechService, persistence)

    for {
      speechServiceRepositoryControls <- BukkitRepositoryControls.createHandles(
        RepositoryDefinition
          .Phased
          .TwoPhased(
            SpeechServiceRepositoryDefinitions.initialization[SyncIO, Player],
            SpeechServiceRepositoryDefinitions.finalization[SyncIO, Player]
          )
      )
      _fairyRoutine = new BukkitFairyRoutine(
        fairySpeechProvider(speechServiceRepositoryControls.repository)
      )
      fairyRecoveryRoutineFiberRepositoryControls <- BukkitRepositoryControls.createHandles(
        RepositoryDefinition
          .Phased
          .TwoPhased(
            FairyManaRecoveryRoutineFiberRepositoryDefinition
              .initialization[Player](_fairyRoutine),
            FairyManaRecoveryRoutineFiberRepositoryDefinition.finalization[SyncIO, Player]
          )
      )
    } yield {
      new System[IO, SyncIO, Player] {
        val fairySpeechServiceRepository: PlayerDataRepository[FairySpeechService[SyncIO]] =
          speechServiceRepositoryControls.repository
        val fairySpeech: FairySpeech[IO, Player] = fairySpeechProvider(
          fairySpeechServiceRepository
        )

        override implicit val api: FairyAPI[IO, SyncIO, Player] =
          new FairyAPI[IO, SyncIO, Player] {
            override def appleOpenState(uuid: UUID): IO[FairyAppleConsumeStrategy] =
              persistence.appleOpenState(uuid)

            override def updateAppleOpenState(
              uuid: UUID,
              appleOpenState: FairyAppleConsumeStrategy
            ): IO[Unit] =
              persistence.changeAppleOpenState(uuid, appleOpenState)

            override def getFairyLore(uuid: UUID): IO[FairyLore] = for {
              state <- appleOpenState(uuid)
            } yield FairyLoreTable.loreTable(state)

            override def updateFairySummonCost(
              uuid: UUID,
              fairySummonCost: FairySummonCost
            ): IO[Unit] =
              persistence.updateFairySummonCost(uuid, fairySummonCost)

            override def fairySummonCost(player: Player): IO[FairySummonCost] =
              persistence.fairySummonCost(player.getUniqueId)

            override def fairyEndTime(player: Player): IO[Option[FairyEndTime]] =
              persistence.fairyEndTime(player.getUniqueId)

            override def updateFairyEndTime(
              player: Player,
              fairyEndTime: FairyEndTime
            ): IO[Unit] =
              persistence.updateFairyEndTime(player.getUniqueId, fairyEndTime)

            override def isFairyUsing(player: Player): IO[Boolean] =
              persistence.isFairyUsing(player.getUniqueId)

            override def updateIsFairyUsing(player: Player, isFairyUsing: Boolean): IO[Unit] =
              persistence.updateIsFairyUsing(player.getUniqueId, isFairyUsing)

            override def fairyRecoveryMana(uuid: UUID): IO[FairyRecoveryMana] =
              persistence.fairyRecoveryMana(uuid)

            override def updateFairyRecoveryManaAmount(
              uuid: UUID,
              fairyRecoveryMana: FairyRecoveryMana
            ): IO[Unit] = persistence.updateFairyRecoveryMana(uuid, fairyRecoveryMana)

            override def increaseAppleAteByFairy(
              uuid: UUID,
              appleAmount: AppleAmount
            ): IO[Unit] =
              persistence.increaseAppleAteByFairy(uuid, appleAmount)

            override def appleAteByFairy(uuid: UUID): IO[Option[AppleAmount]] =
              persistence.appleAteByFairy(uuid)

            override def appleAteByFairyMyRanking(
              player: Player
            ): IO[Option[AppleAteByFairyRank]] =
              persistence.appleAteByFairyMyRanking(player.getUniqueId)

            override def appleAteByFairyRanking(
              number: Int
            ): IO[Vector[Option[AppleAteByFairyRank]]] =
              persistence.appleAteByFairyRanking(number)

            override def allEatenAppleAmount: IO[AppleAmount] =
              persistence.allEatenAppleAmount

            override def fairySpeechSound(uuid: UUID): IO[Boolean] =
              persistence.fairySpeechSound(uuid)

            override def toggleFairySpeechSound(uuid: UUID): IO[Unit] =
              persistence.toggleFairySpeechSound(uuid, !fairySpeechSound(uuid).unsafeRunSync())

            override def createPlayerData(uuid: UUID): IO[Unit] =
              persistence.createPlayerData(uuid)

            override def speechEndTime(player: Player): IO[Unit] =
              fairySpeech.speechEndTime(player)

            override def summonSpeech(player: Player): IO[Unit] =
              fairySpeech.summonSpeech(player)

            override def welcomeBack(player: Player): IO[Unit] =
              fairySpeech.welcomeBack(player)
          }

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[IO, _]] =
          Seq(speechServiceRepositoryControls, fairyRecoveryRoutineFiberRepositoryControls).map(
            _.coerceFinalizationContextTo[IO]
          )

        override val listeners: Seq[Listener] = Seq(new FairyPlayerJoinGreeter)
      }
    }
  }

}
