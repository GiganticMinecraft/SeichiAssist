package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, IO, SyncIO}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.datarepository.bukkit.player.{
  BukkitRepositoryControls,
  PlayerDataRepository
}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.timer
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.SummonFairy
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.repository.{
  FairyManaRecoveryRoutineFiberRepositoryDefinition,
  SpeechServiceRepositoryDefinitions
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions.BukkitSummonFairy
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.listeners.FairyPlayerJoinGreeter
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.routines.BukkitFairyRoutine
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.{
  BukkitFairySpeech,
  BukkitFairySummonRequest
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property._
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.speech.FairySpeech
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.{
  FairyPersistence,
  FairySpawnRequestErrorOrSpawn,
  FairySummonRequest
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.infrastructure.JdbcFairyPersistence
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech.bukkit.BukkitFairySpeechGateway
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech.domain.FairySpeechGateway
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech.service.FairySpeechService
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

import java.util.UUID

trait System[F[_], G[_], Player] extends Subsystem[F] {
  val api: FairyAPI[F, G, Player]
}

object System {

  def wired(
    implicit breakCountAPI: BreakCountAPI[IO, SyncIO, Player],
    voteAPI: VoteAPI[IO, Player],
    manaApi: ManaApi[IO, SyncIO, Player],
    mineStackAPI: MineStackAPI[IO, Player, ItemStack],
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
            override def consumeStrategy(uuid: UUID): IO[FairyAppleConsumeStrategy] =
              persistence.appleConsumeStrategy(uuid)

            override def updateAppleOpenState(
              appleConsumeStrategy: FairyAppleConsumeStrategy
            ): Kleisli[IO, Player, Unit] = Kleisli { player =>
              persistence.updateAppleConsumeStrategy(player.getUniqueId, appleConsumeStrategy)
            }

            override def updateFairySummonCost(
              fairySummonCost: FairySummonCost
            ): Kleisli[IO, Player, Unit] = Kleisli { player =>
              persistence.updateFairySummonCost(player.getUniqueId, fairySummonCost)
            }

            override def fairySummonCost(player: Player): IO[FairySummonCost] =
              persistence.fairySummonCost(player.getUniqueId)

            override def isFairyAppearing(player: Player): IO[Boolean] =
              persistence.isFairyUsing(player.getUniqueId)

            override def rankByMostConsumedApple(
              player: Player
            ): IO[Option[AppleConsumeAmountRank]] =
              persistence.rankByConsumedAppleAmountByFairy(player.getUniqueId)

            override def rankingByMostConsumedApple(
              top: Int
            ): IO[Vector[Option[AppleConsumeAmountRank]]] =
              persistence.fetchMostConsumedApplePlayersByFairy(top)

            override def totalConsumedApple: IO[AppleAmount] =
              persistence.totalConsumedAppleAmount

            override def doPlaySoundOnSpeak(uuid: UUID): IO[Boolean] =
              persistence.playSoundOnFairySpeech(uuid)

            override def toggleSoundOnSpeak: Kleisli[IO, Player, Unit] = Kleisli { player =>
              val uuid = player.getUniqueId
              for {
                isPlayFairySpeechSound <- doPlaySoundOnSpeak(uuid)
                _ <- persistence.setPlaySoundOnSpeech(uuid, !isPlayFairySpeechSound)
              } yield ()
            }

            override def sendDisappearTimeToChat: Kleisli[IO, Player, Unit] = Kleisli {
              player => fairySpeech.speechEndTime(player)
            }

            override def fairySummonRequest
              : Kleisli[IO, Player, FairySpawnRequestErrorOrSpawn[IO]] = Kleisli { player =>
              summonRequest.summonRequest(player)
            }

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
