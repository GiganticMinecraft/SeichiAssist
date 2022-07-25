package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy

import cats.effect.{ConcurrentEffect, Sync}
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.bukkit.FairyLoreTable
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain._
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.infrastructure.JdbcFairyPersistence

import java.util.UUID

trait System[F[_]] extends Subsystem[F] {
  val api: FairyAPI[F]
}

object System {

  def wired[F[_]: ConcurrentEffect: OnMinecraftServerThread]: System[F] = {
    val persistence = new JdbcFairyPersistence[F]
    new System[F] {
      override val api: FairyAPI[F] = new FairyAPI[F] {
        override def appleOpenState(uuid: UUID): F[AppleOpenState] =
          persistence.appleOpenState(uuid)

        override def updateAppleOpenState(uuid: UUID, appleOpenState: AppleOpenState): F[Unit] =
          persistence.changeAppleOpenState(uuid, appleOpenState)

        import cats.implicits._

        override def getFairyLore(uuid: UUID): F[FairyLore] = for {
          state <- appleOpenState(uuid)
        } yield FairyLoreTable.loreTable(state.amount - 1)

        override def updateFairySummonState(
          uuid: UUID,
          validTimeState: FairyValidTimeState
        ): F[Unit] =
          persistence.updateFairyValidTimeState(uuid, validTimeState)

        override def fairyValidTimeState(uuid: UUID): F[FairyValidTimeState] =
          persistence.fairySummonState(uuid)

        override protected val fairyPlaySoundRepository
          : KeyedDataRepository[UUID, Ref[F, FairyPlaySound]] =
          KeyedDataRepository.unlift[UUID, Ref[F, FairyPlaySound]] { _ =>
            Some(Ref.unsafe(FairyPlaySound.play))
          }

        override def fairyPlaySound(uuid: UUID): F[FairyPlaySound] =
          if (fairyPlaySoundRepository.isDefinedAt(uuid))
            fairyPlaySoundRepository(uuid).get
          else Sync[F].pure(FairyPlaySound.play)

        override def fairyPlaySoundToggle(uuid: UUID): F[Unit] = for {
          nowSetting <- fairyPlaySound(uuid)
        } yield fairyPlaySoundRepository(uuid).set(
          if (nowSetting == FairyPlaySound.play) FairyPlaySound.notPlay else FairyPlaySound.play
        )

        override protected[this] val fairyValidTimeRepository
          : KeyedDataRepository[UUID, Ref[F, Option[FairyValidTimes]]] =
          KeyedDataRepository.unlift[UUID, Ref[F, Option[FairyValidTimes]]](_ => None)

        override def fairyValidTimes(uuid: UUID): F[Option[FairyValidTimes]] =
          fairyValidTimeRepository(uuid).get

        override def updateFairyValidTimes(
          uuid: UUID,
          fairyValidTimes: Option[FairyValidTimes]
        ): F[Unit] = fairyValidTimeRepository(uuid).set(fairyValidTimes)

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
