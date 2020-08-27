package com.github.unchama.seichiassist.subsystems.itemmigration

import cats.effect.{ConcurrentEffect, ContextShift, IO, Sync, SyncEffect, SyncIO}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.itemmigration.controllers.player.{PlayerItemMigrationController, PlayerItemMigrationStateRepository}
import com.github.unchama.itemmigration.service
import com.github.unchama.itemmigration.service.ItemMigrationService
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems.itemmigration.domain.minecraft.UuidRepository
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.loggers.{PersistedItemsMigrationSlf4jLogger, PlayerItemsMigrationSlf4jLogger, WorldLevelMigrationSlf4jLogger}
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.minecraft.JdbcBackedUuidRepository
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.repositories.{PersistedItemsMigrationVersionRepository, PlayerItemsMigrationVersionRepository, WorldLevelItemsMigrationVersionRepository}
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.targets.{SeichiAssistPersistedItems, SeichiAssistWorldLevelData}
import com.github.unchama.seichiassist.subsystems.itemmigration.migrations.SeichiAssistItemMigrations
import org.slf4j.Logger
import scalikejdbc.DB

object System {

  import cats.effect.implicits._

  private def migrations(implicit logger: Logger) = {
    implicit val uuidRepository: UuidRepository[SyncIO] =
      JdbcBackedUuidRepository.initializeInstance[SyncIO].unsafeRunSync()

    SeichiAssistItemMigrations.seq
  }

  def wired[
    F[_] : ConcurrentEffect : ContextShift,
    G[_] : SyncEffect : ContextCoercion[*[_], F]
  ](implicit effectEnvironment: EffectEnvironment, logger: Logger): G[StatefulSubsystem[InternalState[F]]] = Sync[G].delay {

    val service = ItemMigrationService.inContextOf[F](
      new PlayerItemsMigrationVersionRepository(SeichiAssist.seichiAssistConfig.getServerId),
      new PlayerItemsMigrationSlf4jLogger(logger)
    )

    val repository = new PlayerItemMigrationStateRepository[F, G, F]
    val controller = new PlayerItemMigrationController[F, G](repository, migrations, service)

    StatefulSubsystem(
      listenersToBeRegistered = Seq(
        repository,
        controller
      ),
      commandsToBeRegistered = Map(),
      stateToExpose = InternalState(repository)
    )
  }

  def runDatabaseMigration[
    F[_] : SyncEffect
  ](implicit effectEnvironment: EffectEnvironment, logger: Logger): F[Unit] = Sync[F].delay {
    DB.autoCommit { implicit session =>
      // DB内アイテムのマイグレーション
      ItemMigrationService.inContextOf[F](
        new PersistedItemsMigrationVersionRepository(),
        new PersistedItemsMigrationSlf4jLogger(logger)
      )
        .runMigration(migrations)(new SeichiAssistPersistedItems())
        .runSync[SyncIO]
        .unsafeRunSync()
    }
  }

  def runWorldMigration[
    F[_] : SyncEffect
  ](implicit effectEnvironment: EffectEnvironment, logger: Logger): F[Unit] = Sync[F].delay {
    // ワールド内アイテムのマイグレーション
    // TODO IOを剥がす
    service.ItemMigrationService.inContextOf[IO](
      new WorldLevelItemsMigrationVersionRepository(SeichiAssist.seichiAssistConfig.getServerId),
      new WorldLevelMigrationSlf4jLogger(logger)
    )
      .runMigration(migrations) {
        import PluginExecutionContexts.asyncShift
        new SeichiAssistWorldLevelData()
      }
      .unsafeRunSync()
  }

}
