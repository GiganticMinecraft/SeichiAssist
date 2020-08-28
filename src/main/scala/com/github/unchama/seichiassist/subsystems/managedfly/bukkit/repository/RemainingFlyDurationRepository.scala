package com.github.unchama.seichiassist.subsystems.managedfly.bukkit.repository

import java.util.UUID

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, ContextShift, SyncEffect}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.playerdatarepository.PreLoginToQuitPlayerDataRepository
import com.github.unchama.seichiassist.subsystems.managedfly.domain.RemainingFlyDuration
import org.bukkit.entity.Player

class RemainingFlyDurationRepository[
  AsyncContext[_] : ConcurrentEffect : ContextShift,
  SyncContext[_] : SyncEffect : ContextCoercion[*[_], AsyncContext]
](implicit effectEnvironment: EffectEnvironment)
  extends PreLoginToQuitPlayerDataRepository[AsyncContext, SyncContext, Ref[SyncContext, Option[RemainingFlyDuration]]] {

  import cats.implicits._

  override protected val loadData: (String, UUID) => SyncContext[Either[Option[String], Ref[SyncContext, Option[RemainingFlyDuration]]]] =
    (_, _) => {
      for {
        ref <- Ref.of[SyncContext, Option[RemainingFlyDuration]](None)
      } yield {
        Right(ref)
      }
    }

  override protected val unloadData: (Player, Ref[SyncContext, Option[RemainingFlyDuration]]) => SyncContext[Unit] =
    (_, _) => SyncEffect[SyncContext].unit

}
