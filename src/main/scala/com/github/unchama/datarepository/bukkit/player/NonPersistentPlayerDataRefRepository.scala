package com.github.unchama.datarepository.bukkit.player

import java.util.UUID

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, ContextShift, Sync, SyncEffect}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import org.bukkit.entity.Player

class NonPersistentPlayerDataRefRepository[
  DataAccessContext[_] : Sync,
  AsyncContext[_] : ConcurrentEffect : ContextShift,
  SyncContext[_] : SyncEffect : ContextCoercion[*[_], AsyncContext],
  D
](initial: D)(implicit effectEnvironment: EffectEnvironment)
  extends PreLoginToQuitPlayerDataRepository[AsyncContext, SyncContext, Ref[DataAccessContext, D]] {

  import cats.implicits._

  override val loadData: (String, UUID) => SyncContext[Either[Option[String], Ref[DataAccessContext, D]]] =
    (_, _) => Ref.in[SyncContext, DataAccessContext, D](initial).map(Right(_))

  override val unloadData: (Player, Ref[DataAccessContext, D]) => SyncContext[Unit] =
    (_, _) => SyncEffect[SyncContext].unit
}
