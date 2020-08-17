package com.github.unchama.itemmigration.controllers.player

import java.util.UUID

import cats.effect.concurrent.{Deferred, TryableDeferred}
import cats.effect.{Concurrent, IO, SyncIO}
import com.github.unchama.playerdatarepository.PreLoginToQuitPlayerDataRepository
import org.bukkit.entity.Player

/**
 * 各プレーヤーのマイグレーション処理の状態を保持するオブジェクトのクラス。
 */
class PlayerItemMigrationStateRepository[F[_]](implicit F: Concurrent[F])
  extends PreLoginToQuitPlayerDataRepository[TryableDeferred[F, Unit]] {

  override val loadData: (String, UUID) => SyncIO[Either[Option[String], TryableDeferred[F, Unit]]] =
    (_, _) => {
      SyncIO {
        Deferred.unsafe[F, Unit]
      }.map {
        promise => Right(promise.asInstanceOf[TryableDeferred[F, Unit]])
      }
    }

  override val unloadData: (Player, TryableDeferred[F, Unit]) => IO[Unit] = (_, _) => IO.unit

}
