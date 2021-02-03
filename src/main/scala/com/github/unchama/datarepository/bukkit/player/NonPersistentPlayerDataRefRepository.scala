package com.github.unchama.datarepository.bukkit.player

import cats.effect.concurrent.Ref
import cats.effect.{Sync, SyncEffect}
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import org.bukkit.entity.Player

import java.util.UUID

@deprecated("Move to BukkitRepositoryControls for compositionality")
class NonPersistentPlayerDataRefRepository[
  DataAccessContext[_] : Sync,
  SyncContext[_] : SyncEffect,
  D
](initial: D)(implicit effectEnvironment: EffectEnvironment)
  extends PreLoginToQuitPlayerDataRepository[SyncContext, Ref[DataAccessContext, D]] {

  import cats.implicits._

  override val loadData: (String, UUID) => SyncContext[Either[Option[String], Ref[DataAccessContext, D]]] =
    (_, _) => Ref.in[SyncContext, DataAccessContext, D](initial).map(Right(_))

  override val finalizeBeforeUnload: (Player, Ref[DataAccessContext, D]) => SyncContext[Unit] =
    (_, _) => SyncEffect[SyncContext].unit
}
