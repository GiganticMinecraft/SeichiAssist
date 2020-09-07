package com.github.unchama.seichiassist.subsystems.managedfly

import cats.Monad
import cats.effect.{ConcurrentEffect, SyncEffect, Timer}
import com.github.unchama.concurrent.{MinecraftServerThreadShift, ReadOnlyRef}
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems.managedfly.application.SystemConfiguration
import com.github.unchama.seichiassist.subsystems.managedfly.bukkit.BukkitFlySessionGatewayRepository
import com.github.unchama.seichiassist.subsystems.managedfly.domain.PlayerFlyStatus

/**
 * NOTE: このサブシステム(managedfly)は本来BuildAssist側に属するが、
 * BuildAssistがSeichiAssistのサブシステムとして完全に整理されなおすまでは、
 * SeichiAssist直属のサブシステムとして扱う。
 */
object System {

  def wired[
    AsyncContext[_] : ConcurrentEffect : MinecraftServerThreadShift : Timer,
    SyncContext[_] : SyncEffect : ContextCoercion[*[_], AsyncContext]
  ](configuration: SystemConfiguration)(implicit effectEnvironment: EffectEnvironment)
  : SyncContext[StatefulSubsystem[InternalState[SyncContext]]] =
    SyncEffect[SyncContext].delay {
      implicit val _configuration: SystemConfiguration = configuration

      val repository: BukkitFlySessionGatewayRepository[AsyncContext, SyncContext] = {
        new BukkitFlySessionGatewayRepository[AsyncContext, SyncContext]()
      }

      val exposedRepository: PlayerDataRepository[ReadOnlyRef[SyncContext, PlayerFlyStatus]] = {
        Monad[PlayerDataRepository].map(repository) { sessionRef =>
          ReadOnlyRef.fromAnySource(sessionRef.getLatestFlyStatus)
        }
      }

      StatefulSubsystem(
        listenersToBeRegistered = Seq(repository),
        commandsToBeRegistered = Map(),
        stateToExpose = InternalState(exposedRepository)
      )
    }
}
