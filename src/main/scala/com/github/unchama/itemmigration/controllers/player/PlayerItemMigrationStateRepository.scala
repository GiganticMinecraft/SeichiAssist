package com.github.unchama.itemmigration.controllers.player

import java.util.UUID

import cats.effect.concurrent.Deferred
import cats.effect.{Concurrent, IO, SyncIO}
import com.github.unchama.generic.effect.TryableFiber
import com.github.unchama.itemmigration.controllers.player.PlayerItemMigrationStateRepository.PlayerItemMigrationFiber
import com.github.unchama.itemmigration.domain.{ItemMigrations, VersionedItemMigrationExecutor}
import com.github.unchama.itemmigration.targets.PlayerInventoriesData
import com.github.unchama.playerdatarepository.PlayerDataOnMemoryRepository
import org.bukkit.entity.Player

/**
 * 各プレーヤーのマイグレーション処理の状態を保持するオブジェクトのクラス。
 */
class PlayerItemMigrationStateRepository(migrations: ItemMigrations,
                                         executor: VersionedItemMigrationExecutor[IO, PlayerInventoriesData])
                                        (implicit concurrentIO: Concurrent[IO])
  extends PlayerDataOnMemoryRepository[PlayerItemMigrationFiber] {

  override val loadData: (String, UUID) => SyncIO[Either[Option[String], PlayerItemMigrationFiber]] =
    (_, _) => {
      for {
        playerPromise <- Deferred.in[SyncIO, IO, Player]
        migrationProcessFiber <- SyncIO {
          TryableFiber.start {
            for {
              player <- playerPromise.get
              _ <- executor.runMigration(migrations)(PlayerInventoriesData(player))
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

object PlayerItemMigrationStateRepository {

  /**
   * プレーヤーのアイテムマイグレーションのプロセスそのものへの参照。
   *
   * このtraitを持つオブジェクトは、往々にして`Player` のインスタンスがあって初めて処理を続行できる。
   * `resumeWith` にて、処理を続行するために必要な `Player` をプロセスに渡すことができる。
   */
  private trait PlayerItemMigrationFiber {
    /**
     * マイグレーション処理を `player` にて続行する `IO` を返す。
     */
    def resumeWith(player: Player): IO[Unit]

    /**
     * マイグレーション処理への参照
     */
    val fiber: TryableFiber[IO, Unit]
  }

}
