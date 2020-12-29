package com.github.unchama.seichiassist.subsystems.itemmigration

import cats.effect.{ConcurrentEffect, ContextShift, IO, Sync, SyncEffect, SyncIO}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.itemmigration.bukkit.controllers.player.{PlayerItemMigrationController, PlayerItemMigrationStateRepository}
import com.github.unchama.itemmigration.service.ItemMigrationService
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems.itemmigration.controllers.{DatabaseMigrationController, WorldMigrationController}
import com.github.unchama.seichiassist.subsystems.itemmigration.domain.minecraft.UuidRepository
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.loggers.PlayerItemsMigrationSlf4jLogger
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.minecraft.JdbcBackedUuidRepository
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.repositories.PlayerItemsMigrationVersionRepository
import com.github.unchama.seichiassist.subsystems.itemmigration.migrations.SeichiAssistItemMigrations
import org.slf4j.Logger

object System {

  def wired[
    F[_] : ConcurrentEffect : ContextShift,
    G[_] : SyncEffect : ContextCoercion[*[_], F],
    H[_]
  ](implicit effectEnvironment: EffectEnvironment, logger: Logger): G[StatefulSubsystem[H, InternalState[F]]] = Sync[G].delay {

    val migrations = {
      implicit val syncIOUuidRepository: UuidRepository[SyncIO] = JdbcBackedUuidRepository
        .initializeStaticInstance[SyncIO]
        .unsafeRunSync()
        .apply[SyncIO]

      SeichiAssistItemMigrations.seq
    }

    val service = ItemMigrationService.inContextOf[F](
      new PlayerItemsMigrationVersionRepository(SeichiAssist.seichiAssistConfig.getServerId),
      new PlayerItemsMigrationSlf4jLogger(logger)
    )

    val repository = new PlayerItemMigrationStateRepository[G, F]
    val playerItemMigrationController = new PlayerItemMigrationController[F, G](repository, migrations, service)

    val entryPoints = new EntryPoints {
      override def runDatabaseMigration[I[_] : SyncEffect]: I[Unit] = {
        DatabaseMigrationController[I](migrations).runDatabaseMigration
      }

      override def runWorldMigration: IO[Unit] = {
        WorldMigrationController(migrations).runWorldMigration
      }
    }

    StatefulSubsystem(
      listenersToBeRegistered = Seq(
        repository,
        playerItemMigrationController
      ),
      finalizersToBeManaged = Nil,
      commandsToBeRegistered = Map(),
      stateToExpose = InternalState(entryPoints, repository)
    )
  }

}
