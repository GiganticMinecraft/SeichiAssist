package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Sync, SyncIO}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.{
  BukkitRepositoryControls,
  PlayerDataRepository
}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.repository.SpeechServiceRepositoryDefinitions
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.gateway.BukkitFairySpeechGateway
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

  def wired[F[_]: ConcurrentEffect: OnMinecraftServerThread]: SyncIO[System[F, Player]] = {
    val persistence = new JdbcFairyPersistence[F]
    implicit val fairySpeechGatewayProvider: Player => FairySpeechGateway[SyncIO] =
      new BukkitFairySpeechGateway(_)

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
      new System[F, Player] {
        override val api: FairyAPI[F, Player] = new FairyAPI[F, Player] {
          override def appleOpenState(uuid: UUID): F[AppleOpenState] =
            persistence.appleOpenState(uuid)

          override def updateAppleOpenState(
            uuid: UUID,
            appleOpenState: AppleOpenState
          ): F[Unit] =
            persistence.changeAppleOpenState(uuid, appleOpenState)

          import cats.implicits._

          override def getFairyLore(uuid: UUID): F[FairyLore] = for {
            state <- appleOpenState(uuid)
          } yield FairyLoreTable.loreTable(state.amount - 1)

          override def updateFairySummonCost(
            uuid: UUID,
            fairySummonCost: FairySummonCost
          ): F[Unit] =
            persistence.updateFairySummonCost(uuid, fairySummonCost)

          override def fairySummonCost(player: Player): F[FairySummonCost] =
            persistence.fairySummonCost(player.getUniqueId)

          override protected val fairyPlaySoundRepository
            : KeyedDataRepository[UUID, Ref[F, FairyPlaySound]] =
            KeyedDataRepository.unlift[UUID, Ref[F, FairyPlaySound]] { _ =>
              Some(Ref.unsafe(FairyPlaySound.on))
            }

          override def fairyPlaySound(uuid: UUID): F[FairyPlaySound] =
            if (fairyPlaySoundRepository.isDefinedAt(uuid))
              fairyPlaySoundRepository(uuid).get
            else Sync[F].pure(FairyPlaySound.on)

          override def fairyPlaySoundToggle(uuid: UUID): F[Unit] = for {
            nowSetting <- fairyPlaySound(uuid)
          } yield fairyPlaySoundRepository(uuid).set(
            if (nowSetting == FairyPlaySound.on) FairyPlaySound.off
            else FairyPlaySound.on
          )

          override def fairyEndTime(player: Player): F[Option[FairyEndTime]] =
            persistence.fairyEndTime(player.getUniqueId)

          override def updateFairyEndTime(player: Player, fairyEndTime: FairyEndTime): F[Unit] =
            persistence.updateFairyEndTime(player.getUniqueId, fairyEndTime)

          override def fairyUsingState(player: Player): F[FairyUsingState] =
            persistence.fairyUsingState(player.getUniqueId)

          override def updateFairyUsingState(
            player: Player,
            fairyUsingState: FairyUsingState
          ): F[Unit] =
            persistence.updateFairyUsingState(player.getUniqueId, fairyUsingState)

          override def fairyRecoveryMana(uuid: UUID): F[FairyRecoveryMana] =
            persistence.fairyRecoveryMana(uuid)

          override def updateFairyRecoveryManaAmount(
            uuid: UUID,
            fairyRecoveryMana: FairyRecoveryMana
          ): F[Unit] = persistence.updateFairyRecoveryMana(uuid, fairyRecoveryMana)

          override val fairySpeechServiceRepository
            : PlayerDataRepository[FairySpeechService[SyncIO]] =
            speechServiceRepositoryControls.repository
        }

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] =
          Seq(speechServiceRepositoryControls).map(_.coerceFinalizationContextTo[F])

      }
    }
  }

}
