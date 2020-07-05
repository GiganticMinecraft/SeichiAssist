package com.github.unchama.itemmigration.target.player

import java.util.UUID

import cats.effect.concurrent.Deferred
import cats.effect.{CancelToken, Concurrent, IO, SyncIO}
import com.github.unchama.generic.effect.TryableFiber
import com.github.unchama.itemmigration.{ItemMigration, ItemMigrationPersistence, ItemMigrationSeq}
import com.github.unchama.playerdatarepository.PlayerDataOnMemoryRepository
import org.bukkit.entity.Player

class PlayerItemMigrationProgress(migrationSeq: ItemMigrationSeq,
                                  persistence: ItemMigrationPersistence[IO, UUID])
                                 (implicit concurrentIO: Concurrent[IO])
  extends PlayerDataOnMemoryRepository[PlayerItemMigrationFiber] {

  override val loadData: (String, UUID) => SyncIO[Either[Option[String], PlayerItemMigrationFiber]] =
    (_, uuid) => {
      val sortedMigrationSeq = migrationSeq.sortedMigrations

      for {
        filteredMigrationSequence <- SyncIO {
          persistence.filterRequiredMigrations(uuid)(sortedMigrationSeq).unsafeRunSync()
        }
        playerPromise <- Deferred.in[SyncIO, IO, Player]

        migrationProcess: IO[Unit] =
        for {
          playerInstance <- playerPromise.get
          inventoryData = new PlayerInventoriesData(playerInstance)
          unifiedConversion = ItemMigration.toSingleFunction(filteredMigrationSequence)
          _ <- inventoryData.runMigration(unifiedConversion)
          _ <- persistence.writeCompletedMigrations(playerInstance.getUniqueId)(filteredMigrationSequence)
        } yield ()

        migrationProcessFiber <- SyncIO {
          TryableFiber.start(migrationProcess).unsafeRunSync()
        }
      } yield {
        Right {
          new PlayerItemMigrationFiber {
            override def invokeWith(player: Player): IO[Unit] = playerPromise.complete(player)

            override def isComplete: IO[Boolean] = migrationProcessFiber.isComplete

            override val cancel: CancelToken[IO] = migrationProcessFiber.cancel
          }
        }
      }
    }

  override val unloadData: (Player, PlayerItemMigrationFiber) => IO[Unit] = (_, d) => d.cancel

}
