package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Sync, SyncEffect}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.bukkit.FairyLoreTable
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property._
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.infrastructure.JdbcFairyPersistence
import org.bukkit.entity.Player

import java.util.UUID

trait System[F[_], Player] extends Subsystem[F] {
  val api: FairyAPI[F, Player]
}

object System {

  def wired[F[_]: ConcurrentEffect: OnMinecraftServerThread, G[_]: SyncEffect: ContextCoercion[
    *[_],
    F
  ]]: System[F, Player] = {
    val persistence = new JdbcFairyPersistence[F]
    new System[F, Player] {
      override val api: FairyAPI[F, Player] = new FairyAPI[F, Player] {
        override def appleOpenState(uuid: UUID): F[AppleOpenState] =
          persistence.appleOpenState(uuid)

        override def updateAppleOpenState(uuid: UUID, appleOpenState: AppleOpenState): F[Unit] =
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

        override def fairyEndTime(player: Player): F[Option[FairyEndTime]] =
          persistence.fairyEndTime(player.getUniqueId)

        override def updateFairyEndTime(player: Player, fairyEndTime: FairyEndTime): F[Unit] =
          persistence.updateFairyEndTime(player.getUniqueId, fairyEndTime)

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
    }
  }

}
