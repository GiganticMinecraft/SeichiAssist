package com.github.unchama.seichiassist.subsystems.itemmigration

import cats.effect.concurrent.TryableDeferred
import cats.effect.{ConcurrentEffect, ContextShift, IO, Sync, SyncEffect, SyncIO}
import com.github.unchama.bungeesemaphoreresponder.domain.PlayerDataFinalizer
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.itemmigration.bukkit.controllers.player.{PlayerItemMigrationController, PlayerItemMigrationStateRepository}
import com.github.unchama.itemmigration.service.ItemMigrationService
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.itemmigration.controllers.{DatabaseMigrationController, WorldMigrationController}
import com.github.unchama.seichiassist.subsystems.itemmigration.domain.minecraft.UuidRepository
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.loggers.PlayerItemsMigrationSlf4jLogger
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.minecraft.JdbcBackedUuidRepository
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.repositories.PlayerItemsMigrationVersionRepository
import com.github.unchama.seichiassist.subsystems.itemmigration.migrations.SeichiAssistItemMigrations
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.slf4j.Logger

trait System[F[_], H[_]] extends Subsystem[H] {
  val entryPoints: EntryPoints
  val migrationStateRepository: PlayerDataRepository[TryableDeferred[F, Unit]]
}

object System {

  def wired[
    F[_] : ConcurrentEffect : ContextShift,
    G[_] : SyncEffect : ContextCoercion[*[_], F],
    H[_]
  ](implicit effectEnvironment: EffectEnvironment, logger: Logger): G[System[F, H]] = Sync[G].delay {

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

    val systemEntryPoints = new EntryPoints {
      override def runDatabaseMigration[I[_] : SyncEffect]: I[Unit] = {
        DatabaseMigrationController[I](migrations).runDatabaseMigration
      }

      override def runWorldMigration: IO[Unit] = {
        WorldMigrationController(migrations).runWorldMigration
      }
    }

    new System[F, H] {
      override val entryPoints: EntryPoints = systemEntryPoints
      override val migrationStateRepository: PlayerDataRepository[TryableDeferred[F, Unit]] = repository
      override val listeners: Seq[Listener] = Seq(repository, playerItemMigrationController)
      override val managedFinalizers: Seq[PlayerDataFinalizer[H, Player]] = Nil
      override val commands: Map[String, TabExecutor] = Map()
    }
  }

}
