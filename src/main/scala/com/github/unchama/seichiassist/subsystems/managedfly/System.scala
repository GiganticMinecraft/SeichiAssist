package com.github.unchama.seichiassist.subsystems.managedfly

import cats.Monad
import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, SyncEffect, Timer}
import com.github.unchama.concurrent.{MinecraftServerThreadShift, NonServerThreadContextShift, ReadOnlyRef}
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems.managedfly.application.{ActiveSessionFactory, PlayerFlyStatusManipulation, SystemConfiguration}
import com.github.unchama.seichiassist.subsystems.managedfly.bukkit.BukkitPlayerFlyStatusManipulation
import com.github.unchama.seichiassist.subsystems.managedfly.bukkit.controllers.{BukkitActiveFlySessionReferenceRepository, BukkitFlyCommand}
import com.github.unchama.seichiassist.subsystems.managedfly.domain.PlayerFlyStatus
import org.bukkit.entity.Player

/**
 * NOTE: このサブシステム(managedfly)は本来BuildAssist側に属するが、
 * BuildAssistがSeichiAssistのサブシステムとして完全に整理されなおすまでは、
 * SeichiAssist直属のサブシステムとして扱う。
 */
object System {

  def wired[
    AsyncContext[_] : ConcurrentEffect : MinecraftServerThreadShift : NonServerThreadContextShift : Timer,
    SyncContext[_] : SyncEffect : ContextCoercion[*[_], AsyncContext]
  ](configuration: SystemConfiguration)(implicit effectEnvironment: EffectEnvironment)
  : SyncContext[StatefulSubsystem[InternalState[SyncContext]]] = {
    implicit val _configuration: SystemConfiguration = configuration

    implicit val _playerKleisliManipulation: PlayerFlyStatusManipulation[Kleisli[AsyncContext, Player, *]] =
      new BukkitPlayerFlyStatusManipulation[AsyncContext]

    implicit val _factory: ActiveSessionFactory[AsyncContext, Player] =
      new ActiveSessionFactory[AsyncContext, Player]()

    SyncEffect[SyncContext].delay {
      implicit val _stateRepository: BukkitActiveFlySessionReferenceRepository[AsyncContext, SyncContext] =
        new BukkitActiveFlySessionReferenceRepository[AsyncContext, SyncContext]()

      val exposedRepository: PlayerDataRepository[ReadOnlyRef[SyncContext, PlayerFlyStatus]] = {
        Monad[PlayerDataRepository].map(_stateRepository) { sessionRef =>
          ReadOnlyRef.fromAnySource(sessionRef.getLatestFlyStatus)
        }
      }

      StatefulSubsystem(
        listenersToBeRegistered = Seq(_stateRepository),
        commandsToBeRegistered = Map(
          "fly" -> BukkitFlyCommand.executor[AsyncContext, SyncContext]
        ),
        stateToExpose = InternalState(exposedRepository)
      )
    }
  }
}
