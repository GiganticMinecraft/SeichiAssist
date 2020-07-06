package com.github.unchama.itemmigration.target.player

import java.util.UUID

import cats.effect.concurrent.Deferred
import cats.effect.{Concurrent, IO, SyncIO}
import com.github.unchama.generic.effect.TryableFiber
import com.github.unchama.itemmigration.{ItemMigration, ItemMigrationPersistence, ItemMigrationSeq}
import com.github.unchama.playerdatarepository.PlayerDataOnMemoryRepository
import org.bukkit.entity.Player

/**
 * 各プレーヤーのマイグレーション処理の状態を保持するオブジェクトのクラス。
 */
class PlayerItemMigrationStateRepository(migrationSeq: ItemMigrationSeq,
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
        unifiedConversion = ItemMigration.toSingleFunction(filteredMigrationSequence)

        playerPromise <- Deferred.in[SyncIO, IO, Player]
        migrationProcess: IO[Unit] =
        for {
          playerInstance <- playerPromise.get
          inventoryData = new PlayerInventoriesData(playerInstance)
          _ <- inventoryData.runMigration(unifiedConversion)
          _ <- persistence.writeCompletedMigrations(playerInstance.getUniqueId)(filteredMigrationSequence)
        } yield ()

        migrationProcessFiber <- SyncIO {
          TryableFiber.start(migrationProcess).unsafeRunSync()
        }
      } yield {
        Right {
          new PlayerItemMigrationFiber {
            override def resumeWith(player: Player): IO[Unit] = playerPromise.complete(player)

            override val fiber: TryableFiber[IO, Unit] = migrationProcessFiber
          }
        }
      }
    }

  override val unloadData: (Player, PlayerItemMigrationFiber) => IO[Unit] = (_, f) => f.fiber.cancel

}
