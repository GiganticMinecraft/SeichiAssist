package com.github.unchama.seichiassist.subsystems.itemmigration

import cats.effect.{ConcurrentEffect, ContextShift, IO, Sync, SyncEffect, SyncIO}
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.itemmigration.application.ItemMigrationStateRepositoryDefinitions
import com.github.unchama.itemmigration.bukkit.controllers.player.PlayerItemMigrationController
import com.github.unchama.itemmigration.service.ItemMigrationService
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.itemmigration.controllers.{
  DatabaseMigrationController,
  WorldMigrationController
}
import com.github.unchama.seichiassist.subsystems.itemmigration.domain.minecraft.UuidRepository
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.loggers.PlayerItemsMigrationSlf4jLogger
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.minecraft.JdbcBackedUuidRepository
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.repositories.PlayerItemsMigrationVersionRepository
import com.github.unchama.seichiassist.subsystems.itemmigration.migrations.SeichiAssistItemMigrations
import org.bukkit.event.Listener
import org.slf4j.Logger

import java.util.UUID

trait System[F[_]] extends Subsystem[F] {
  val entryPoints: EntryPoints
}

object System {

  import cats.implicits._

  def wired[F[_]: ConcurrentEffect: ContextShift, G[_]: SyncEffect: ContextCoercion[*[_], F]](
    implicit logger: Logger
  ): G[System[F]] = for {
    migrations <- Sync[G].delay {
      implicit val syncIOUuidRepository: UuidRepository[SyncIO] =
        JdbcBackedUuidRepository.initializeStaticInstance[SyncIO].unsafeRunSync().apply[SyncIO]

      SeichiAssistItemMigrations.seq
    }

    service = ItemMigrationService.inContextOf[F](
      new PlayerItemsMigrationVersionRepository(SeichiAssist.seichiAssistConfig.getServerId),
      new PlayerItemsMigrationSlf4jLogger(logger)
    )

    repositoryControls <-
      BukkitRepositoryControls.createHandles(
        RepositoryDefinition
          .Phased
          .SinglePhased
          .withoutTappingAction(
            ItemMigrationStateRepositoryDefinitions.initialization[G],
            ItemMigrationStateRepositoryDefinitions.finalization[G, UUID]
          )
      )
  } yield {
    val playerItemMigrationController = new PlayerItemMigrationController[F, G](
      repositoryControls.repository,
      migrations,
      service
    )

    new System[F] {
      override val entryPoints: EntryPoints = new EntryPoints {
        override def runDatabaseMigration[I[_]: SyncEffect]: I[Unit] = {
          DatabaseMigrationController[I](migrations).runDatabaseMigration
        }

        override def runWorldMigration: IO[Unit] = {
          WorldMigrationController(migrations).runWorldMigration
        }
      }

      override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] = Seq(
        repositoryControls.coerceFinalizationContextTo[F]
      )

      override val listeners: Seq[Listener] = Seq(playerItemMigrationController)
    }
  }

}
