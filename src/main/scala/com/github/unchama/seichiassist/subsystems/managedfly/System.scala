package com.github.unchama.seichiassist.subsystems.managedfly

import cats.Monad
import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, SyncEffect, Timer}
import com.github.unchama.concurrent.{MinecraftServerThreadShift, NonServerThreadContextShift, ReadOnlyRef}
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems.managedfly.application.{PlayerFlySessionFactory, PlayerFlyStatusManipulation, SystemConfiguration}
import com.github.unchama.seichiassist.subsystems.managedfly.bukkit.{BukkitFlySessionReferenceRepository, BukkitPlayerFlyStatusManipulation}
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
  : SyncContext[StatefulSubsystem[InternalState[SyncContext]]] =
    SyncEffect[SyncContext].delay {
      implicit val _configuration: SystemConfiguration = configuration

      implicit val _manipulation: PlayerFlyStatusManipulation[Kleisli[AsyncContext, Player, *]] =
        new BukkitPlayerFlyStatusManipulation[AsyncContext]

      implicit val _factory: PlayerFlySessionFactory[AsyncContext, Player] =
        new PlayerFlySessionFactory[AsyncContext, Player]()

      implicit val _stateRepository: BukkitFlySessionReferenceRepository[AsyncContext, SyncContext] =
        new BukkitFlySessionReferenceRepository[AsyncContext, SyncContext]()

      val exposedRepository: PlayerDataRepository[ReadOnlyRef[SyncContext, PlayerFlyStatus]] = {
        Monad[PlayerDataRepository].map(_stateRepository) { sessionRef =>
          ReadOnlyRef.fromAnySource(sessionRef.getLatestFlyStatus)
        }
      }

      StatefulSubsystem(
        listenersToBeRegistered = Seq(_stateRepository),
        commandsToBeRegistered = Map(),
        stateToExpose = InternalState(exposedRepository)
      )
    }
}
