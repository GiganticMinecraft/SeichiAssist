package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy

import cats.effect.{ConcurrentEffect, IO, SyncIO}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.datarepository.bukkit.player.{
  BukkitRepositoryControls,
  PlayerDataRepository
}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.SummonFairy
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.repository.{
  FairyManaRecoveryRoutineFiberRepositoryDefinition,
  SpeechServiceRepositoryDefinitions
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.{
  BukkitFairySpeech,
  BukkitFairySummonRequest
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions.BukkitSummonFairy
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.gateway.BukkitFairySpeechGateway
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.listeners.FairyPlayerJoinGreeter
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.routines.BukkitFairyRoutine
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property._
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.resources.bukkit.FairyLoreTable
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.speech.{
  FairySpeech,
  FairySpeechGateway
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.{
  FairyPersistence,
  FairySpawnRequestErrorOrSpawn,
  FairySummonRequest
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
    concurrentEffect: ConcurrentEffect[IO],
    minecraftServerThread: OnMinecraftServerThread[IO]
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
            FairyManaRecoveryRoutineFiberRepositoryDefinition.finalization[Player]
          )
      )
    } yield {
      new System[IO, SyncIO, Player] {
        val fairySpeechServiceRepository: PlayerDataRepository[FairySpeechService[SyncIO]] =
          speechServiceRepositoryControls.repository
        implicit val fairySpeech: FairySpeech[IO, Player] = fairySpeechProvider(
          fairySpeechServiceRepository
        )
        implicit val summonFairy: SummonFairy[IO, Player] =
          new BukkitSummonFairy[IO, SyncIO]
        val summonRequest: FairySummonRequest[IO, Player] =
          new BukkitFairySummonRequest[IO, SyncIO]

        override implicit val api: FairyAPI[IO, SyncIO, Player] =
          new FairyAPI[IO, SyncIO, Player] {
            override def appleOpenState(uuid: UUID): IO[FairyAppleConsumeStrategy] =
              persistence.appleOpenState(uuid)

            override def updateAppleOpenState(
              uuid: UUID,
              appleConsumeStrategy: FairyAppleConsumeStrategy
            ): IO[Unit] =
              persistence.changeAppleOpenState(uuid, appleConsumeStrategy)

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

            override def isFairyUsing(player: Player): IO[Boolean] =
              persistence.isFairyUsing(player.getUniqueId)

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

            override def isPlayFairySpeechSound(uuid: UUID): IO[Boolean] =
              persistence.fairySpeechSound(uuid)

            override def toggleFairySpeechSound(uuid: UUID): IO[Unit] = for {
              isPlayFairySpeechSound <- isPlayFairySpeechSound(uuid)
            } yield persistence.toggleFairySpeechSound(uuid, !isPlayFairySpeechSound)

            override def speechEndTime(player: Player): IO[Unit] =
              fairySpeech.speechEndTime(player)

            override def fairySummonRequest(
              player: Player
            ): IO[FairySpawnRequestErrorOrSpawn[IO]] =
              summonRequest.summonRequest(player)

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
