package com.github.unchama.buildassist.bukkit.datarepository

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, SyncEffect, Timer}
import com.github.unchama.buildassist.domain.playerdata.{BuildAmountData, BuildAmountDataPersistence}
import com.github.unchama.datarepository.bukkit.player.TwoPhasedPlayerDataRepository
import com.github.unchama.generic.ContextCoercion
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player

import java.util.UUID

class BuildAmountDataRepository[
  F[_] : SyncEffect,
  G[_] : ConcurrentEffect : Timer : ErrorLogger : ContextCoercion[F, *[_]]
](implicit persistence: BuildAmountDataPersistence[F])
  extends TwoPhasedPlayerDataRepository[F, Ref[F, BuildAmountData]] {

  import cats.implicits._

  override protected type TemporaryData = BuildAmountData

  override protected val loadTemporaryData: (String, UUID) => F[Either[Option[String], BuildAmountData]] =
    (_, uuid) =>
      persistence
        .read(uuid)
        .map(_.getOrElse(BuildAmountData.initial))
        .map(Right.apply)

  override protected def initializeValue(player: Player, temporaryData: BuildAmountData): F[Ref[F, BuildAmountData]] =
    Ref.of(temporaryData)

  override protected val finalizeBeforeUnload: (Player, Ref[F, BuildAmountData]) => F[Unit] =
    (player, dataRef) => dataRef.get.flatMap(persistence.write(player.getUniqueId, _))
}
