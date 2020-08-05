package com.github.unchama.itemmigration.controllers.player

import java.util.UUID

import cats.effect.concurrent.Deferred
import cats.effect.{Concurrent, IO, SyncIO}
import com.github.unchama.generic.effect.TryableFiber
import com.github.unchama.itemmigration.domain.ItemMigrations
import com.github.unchama.itemmigration.service.ItemMigrationService
import com.github.unchama.itemmigration.targets.PlayerInventoriesData
import com.github.unchama.playerdatarepository.PreLoginToQuitPlayerDataRepository
import org.bukkit.entity.Player

/**
 * 各プレーヤーのマイグレーション処理の状態を保持するオブジェクトのクラス。
 */
class PlayerItemMigrationStateRepository(migrations: ItemMigrations,
                                         service: ItemMigrationService[IO, PlayerInventoriesData[IO]])
                                        (implicit concurrentIO: Concurrent[IO])
  extends PreLoginToQuitPlayerDataRepository[PlayerItemMigrationFiber] {

  override val loadData: (String, UUID) => SyncIO[Either[Option[String], PlayerItemMigrationFiber]] =
    (_, _) => {
      for {
        playerPromise <- Deferred.in[SyncIO, IO, Player]
        migrationProcessFiber <- SyncIO {
          TryableFiber.start {
            for {
              player <- playerPromise.get
              _ <- service.runMigration(migrations)(PlayerInventoriesData(player))
            } yield ()
          }.unsafeRunSync()
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
