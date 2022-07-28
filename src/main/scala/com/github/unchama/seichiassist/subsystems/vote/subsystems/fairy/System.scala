package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy

import cats.effect.concurrent.Ref
import cats.effect.{IO, SyncIO}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.{
  BukkitRepositoryControls,
  PlayerDataRepository
}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.FairyRoutine
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.repository.{
  FairyManaRecoveryRoutineFiberRepositoryDefinition,
  SpeechServiceRepositoryDefinitions
}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.gateway.BukkitFairySpeechGateway
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.routines.BukkitFairyRoutine
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairySpeechGateway
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.bukkit.FairyLoreTable
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property._
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.infrastructure.JdbcFairyPersistence
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.service.FairySpeechService
import org.bukkit.entity.Player

import java.util.UUID

trait System[F[_], Player] extends Subsystem[F] {
  val api: FairyAPI[F, Player]
}

object System {

  def wired(
    implicit breakCountAPI: BreakCountAPI[IO, SyncIO, Player],
    voteAPI: VoteAPI[IO, Player],
    manaApi: ManaApi[IO, SyncIO, Player],
    context: RepeatingTaskContext
  ): SyncIO[System[IO, Player]] = {
    val persistence = new JdbcFairyPersistence[IO]
    implicit val fairySpeechGatewayProvider: Player => FairySpeechGateway[SyncIO] =
      new BukkitFairySpeechGateway(_)
    implicit val fairyRoutine: FairyRoutine[IO, SyncIO, Player] = BukkitFairyRoutine

    for {
      speechServiceRepositoryControls <- BukkitRepositoryControls.createHandles(
        RepositoryDefinition
          .Phased
          .TwoPhased(
            SpeechServiceRepositoryDefinitions.initialization[SyncIO, Player],
            SpeechServiceRepositoryDefinitions.finalization[SyncIO, Player]
          )
      )
    } yield {
      new System[IO, Player] {
        override implicit val api: FairyAPI[IO, Player] = new FairyAPI[IO, Player] {
          override def appleOpenState(uuid: UUID): IO[AppleOpenState] =
            persistence.appleOpenState(uuid)

          override def updateAppleOpenState(
            uuid: UUID,
            appleOpenState: AppleOpenState
          ): IO[Unit] =
            persistence.changeAppleOpenState(uuid, appleOpenState)

          import cats.implicits._

          override def getFairyLore(uuid: UUID): IO[FairyLore] = for {
            state <- appleOpenState(uuid)
          } yield FairyLoreTable.loreTable(state.amount - 1)

          override def updateFairySummonCost(
            uuid: UUID,
            fairySummonCost: FairySummonCost
          ): IO[Unit] =
            persistence.updateFairySummonCost(uuid, fairySummonCost)

          override def fairySummonCost(player: Player): IO[FairySummonCost] =
            persistence.fairySummonCost(player.getUniqueId)

          override protected val fairyPlaySoundRepository
            : KeyedDataRepository[UUID, Ref[IO, FairyPlaySound]] =
            KeyedDataRepository.unlift[UUID, Ref[IO, FairyPlaySound]] { _ =>
              Some(Ref.unsafe(FairyPlaySound.on))
            }

          override def fairyPlaySound(uuid: UUID): IO[FairyPlaySound] =
            if (fairyPlaySoundRepository.isDefinedAt(uuid))
              fairyPlaySoundRepository(uuid).get
            else IO.pure(FairyPlaySound.on)

          override def fairyPlaySoundToggle(uuid: UUID): IO[Unit] = for {
            nowSetting <- fairyPlaySound(uuid)
          } yield fairyPlaySoundRepository(uuid).set(
            if (nowSetting == FairyPlaySound.on) FairyPlaySound.off
            else FairyPlaySound.on
          )

          override def fairyEndTime(player: Player): IO[Option[FairyEndTime]] =
            persistence.fairyEndTime(player.getUniqueId)

          override def updateFairyEndTime(
            player: Player,
            fairyEndTime: FairyEndTime
          ): IO[Unit] =
            persistence.updateFairyEndTime(player.getUniqueId, fairyEndTime)

          override def fairyUsingState(player: Player): IO[FairyUsingState] =
            persistence.fairyUsingState(player.getUniqueId)

          override def updateFairyUsingState(
            player: Player,
            fairyUsingState: FairyUsingState
          ): IO[Unit] =
            persistence.updateFairyUsingState(player.getUniqueId, fairyUsingState)

          override def fairyRecoveryMana(uuid: UUID): IO[FairyRecoveryMana] =
            persistence.fairyRecoveryMana(uuid)

          override def updateFairyRecoveryManaAmount(
            uuid: UUID,
            fairyRecoveryMana: FairyRecoveryMana
          ): IO[Unit] = persistence.updateFairyRecoveryMana(uuid, fairyRecoveryMana)

          override val fairySpeechServiceRepository
            : PlayerDataRepository[FairySpeechService[SyncIO]] =
            speechServiceRepositoryControls.repository
        }

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[IO, _]] =
          BukkitRepositoryControls
            .createHandles(
              RepositoryDefinition
                .Phased
                .TwoPhased(
                  FairyManaRecoveryRoutineFiberRepositoryDefinition
                    .initialization[SyncIO, Player],
                  FairyManaRecoveryRoutineFiberRepositoryDefinition.finalization[SyncIO, Player]
                )
            )
            .map { fairyRecoveryRoutineFiberRepositoryControls =>
              Seq(speechServiceRepositoryControls, fairyRecoveryRoutineFiberRepositoryControls)
                .map(_.coerceFinalizationContextTo[IO])
            }
            .unsafeRunSync()
      }
    }
  }

}
