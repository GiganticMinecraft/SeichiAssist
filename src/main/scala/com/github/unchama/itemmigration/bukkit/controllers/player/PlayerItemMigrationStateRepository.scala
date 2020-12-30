package com.github.unchama.itemmigration.bukkit.controllers.player

import cats.effect.concurrent.{Deferred, TryableDeferred}
import cats.effect.{Concurrent, SyncEffect}
import com.github.unchama.datarepository.bukkit.player.PreLoginToQuitPlayerDataRepository
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import org.bukkit.entity.Player

import java.util.UUID

/**
 * 各プレーヤーのマイグレーション処理の状態を保持するオブジェクトのクラス。
 */
class PlayerItemMigrationStateRepository[
  SyncContext[_] : SyncEffect,
  DeferredContext[_] : Concurrent
](implicit environment: EffectEnvironment)
  extends PreLoginToQuitPlayerDataRepository[SyncContext, TryableDeferred[DeferredContext, Unit]] {

  import cats.implicits._

  override val loadData: (String, UUID) => SyncContext[Either[Option[String], TryableDeferred[DeferredContext, Unit]]] =
    (_, _) =>
      SyncEffect[SyncContext]
        .delay {
          Deferred
            .unsafe[DeferredContext, Unit]
            .asInstanceOf[TryableDeferred[DeferredContext, Unit]]
        }
        .map(Right.apply)

  override val finalizeBeforeUnload: (Player, TryableDeferred[DeferredContext, Unit]) => SyncContext[Unit] = {
    (_, _) => SyncEffect[SyncContext].unit
  }

}
