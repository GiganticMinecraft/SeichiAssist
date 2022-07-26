package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Sync, SyncEffect}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.{
  BukkitRepositoryControls,
  PlayerDataRepository
}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.repository.FairyValidTimeRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain._
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.bukkit.FairyLoreTable
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.infrastructure.JdbcFairyPersistence
import org.bukkit.entity.Player

import java.util.UUID

trait System[F[_], Player] extends Subsystem[F] {
  val api: FairyAPI[F, Player]
}

object System {

  import cats.implicits._

  def wired[F[_]: ConcurrentEffect: OnMinecraftServerThread, G[_]: SyncEffect: ContextCoercion[
    *[_],
    F
  ]]: F[System[F, Player]] = {
    val persistence = new JdbcFairyPersistence[F]
    implicit val fairyValidTimesState: FairyValidTimesState[G] = new FairyValidTimesState[G]

    for {
      fairyValidTimeRepositoryControls <- ContextCoercion(
        BukkitRepositoryControls.createHandles(
          RepositoryDefinition
            .Phased
            .TwoPhased(
              FairyValidTimeRepositoryDefinition.initialization[G, Player],
              FairyValidTimeRepositoryDefinition.finalization[G, Player]
            )
        )
      )
    } yield {
      new System[F, Player] {
        override val api: FairyAPI[F, Player] = new FairyAPI[F, Player] {

          val repository: PlayerDataRepository[Ref[F, Option[FairyValidTimes]]] =
            fairyValidTimeRepositoryControls
              .repository
              .map(_.mapK[F](ContextCoercion.asFunctionK))

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

          override def fairySummonCost(uuid: UUID): F[FairySummonCost] =
            persistence.fairySummonCost(uuid)

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

          override protected[this] val fairyValidTimeRepository
            : KeyedDataRepository[Player, Ref[F, Option[FairyValidTimes]]] =
            repository

          override def fairyValidTimes(player: Player): F[Option[FairyValidTimes]] =
            fairyValidTimeRepository(player).get

          override def updateFairyValidTimes(
            player: Player,
            fairyValidTimes: Option[FairyValidTimes]
          ): F[Unit] =
            fairyValidTimeRepository(player).set(fairyValidTimes)

          override def fairyUsingState(uuid: UUID): F[FairyUsingState] =
            persistence.fairyUsingState(uuid)

          override def updateFairyUsingState(
            uuid: UUID,
            fairyUsingState: FairyUsingState
          ): F[Unit] =
            persistence.updateFairyUsingState(uuid, fairyUsingState)

          override def fairyRecoveryMana(uuid: UUID): F[FairyRecoveryMana] =
            persistence.fairyRecoveryMana(uuid)

          override def updateFairyRecoveryManaAmount(
            uuid: UUID,
            fairyRecoveryMana: FairyRecoveryMana
          ): F[Unit] = persistence.updateFairyRecoveryMana(uuid, fairyRecoveryMana)
        }
        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] =
          Seq(fairyValidTimeRepositoryControls.coerceFinalizationContextTo[F])
      }
    }
  }

}
