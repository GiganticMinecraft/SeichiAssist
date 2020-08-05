package com.github.unchama.itemmigration.controllers.player

import java.util.UUID

import cats.effect.concurrent.{Deferred, TryableDeferred}
import cats.effect.{Concurrent, IO, SyncIO}
import com.github.unchama.playerdatarepository.PreLoginToQuitPlayerDataRepository
import org.bukkit.entity.Player

/**
 * 各プレーヤーのマイグレーション処理の状態を保持するオブジェクトのクラス。
 */
class PlayerItemMigrationStateRepository(implicit concurrentIO: Concurrent[IO])
  extends PreLoginToQuitPlayerDataRepository[TryableDeferred[IO, Unit]] {

  override val loadData: (String, UUID) => SyncIO[Either[Option[String], TryableDeferred[IO, Unit]]] =
    (_, _) => {
      SyncIO {
        Deferred.unsafe[IO, Unit]
      }.map {
        promise => Right(promise.asInstanceOf[TryableDeferred[IO, Unit]])
      }
    }

  override val unloadData: (Player, TryableDeferred[IO, Unit]) => IO[Unit] = (_, _) => IO.unit

}
