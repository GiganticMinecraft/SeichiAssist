package com.github.unchama.seichiassist.subsystems.managedfly

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, SyncEffect, Timer}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.{
  BukkitRepositoryControls,
  PlayerDataRepository
}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.idletime.IdleTimeAPI
import com.github.unchama.seichiassist.subsystems.managedfly.application._
import com.github.unchama.seichiassist.subsystems.managedfly.application.repository.ActiveSessionReferenceRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.managedfly.bukkit.BukkitPlayerFlyStatusManipulation
import com.github.unchama.seichiassist.subsystems.managedfly.bukkit.controllers.BukkitFlyCommand
import com.github.unchama.seichiassist.subsystems.managedfly.domain.PlayerFlyStatus
import com.github.unchama.seichiassist.subsystems.managedfly.infrastructure.JdbcFlyDurationPersistenceRepository
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

/**
 * NOTE: このサブシステム(managedfly)は本来BuildAssist側に属するが、
 * BuildAssistがSeichiAssistのサブシステムとして完全に整理されなおすまでは、 SeichiAssist直属のサブシステムとして扱う。
 */
trait System[G[_], H[_]] extends Subsystem[H] {
  val api: ManagedFlyApi[G, Player]
}

object System {

  import cats.implicits._

  def wired[AsyncContext[_]: ConcurrentEffect: OnMinecraftServerThread: Timer, SyncContext[
    _
  ]: SyncEffect: ContextCoercion[*[_], AsyncContext]](configuration: SystemConfiguration)(
    implicit idleTimeAPI: IdleTimeAPI[AsyncContext, Player]
  ): SyncContext[System[SyncContext, AsyncContext]] = {
    implicit val _configuration: SystemConfiguration = configuration

    implicit val _jdbcRepository: FlyDurationPersistenceRepository[SyncContext] =
      new JdbcFlyDurationPersistenceRepository[SyncContext]

    implicit val _playerKleisliManipulation
      : PlayerFlyStatusManipulation[Kleisli[AsyncContext, Player, *]] =
      new BukkitPlayerFlyStatusManipulation[AsyncContext]

    implicit val _factory: ActiveSessionFactory[AsyncContext, Player] =
      new ActiveSessionFactory[AsyncContext, Player]()

    import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid._

    BukkitRepositoryControls
      .createHandles(
        ActiveSessionReferenceRepositoryDefinition.withContext(_factory, _jdbcRepository)
      )
      .map { controls =>
        implicit val _repository
          : PlayerDataRepository[ActiveSessionReference[AsyncContext, SyncContext]] =
          controls.repository

        new System[SyncContext, AsyncContext] {
          override val api: ManagedFlyApi[SyncContext, Player] =
            new ManagedFlyApi[SyncContext, Player] {
              override val playerFlyDurations
                : KeyedDataRepository[Player, ReadOnlyRef[SyncContext, PlayerFlyStatus]] =
                controls.repository.map { sessionRef =>
                  ReadOnlyRef.fromAnySource(sessionRef.getLatestFlyStatus)
                }
            }

          override val managedRepositoryControls
            : Seq[BukkitRepositoryControls[AsyncContext, _]] =
            Seq(controls.coerceFinalizationContextTo[AsyncContext])

          override val commands: Map[String, TabExecutor] =
            Map("fly" -> BukkitFlyCommand.executor[AsyncContext, SyncContext])
        }
      }
  }
}
