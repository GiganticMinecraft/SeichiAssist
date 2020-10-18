package com.github.unchama.itemmigration.controllers.player

import java.util.UUID

import cats.effect.concurrent.{Deferred, TryableDeferred}
import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, SyncEffect}
import com.github.unchama.datarepository.bukkit.player.PreLoginToQuitPlayerDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import org.bukkit.entity.Player

/**
 * 各プレーヤーのマイグレーション処理の状態を保持するオブジェクトのクラス。
 */
class PlayerItemMigrationStateRepository[
  AsyncContext[_] : ConcurrentEffect : ContextShift,
  SyncContext[_] : SyncEffect : ContextCoercion[*[_], AsyncContext],
  DeferredContext[_] : Concurrent
](implicit environment: EffectEnvironment)
  extends PreLoginToQuitPlayerDataRepository[AsyncContext, SyncContext, TryableDeferred[DeferredContext, Unit]] {

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

  override val unloadData: (Player, TryableDeferred[DeferredContext, Unit]) => SyncContext[Unit] = {
    (_, _) => SyncEffect[SyncContext].unit
  }

}
