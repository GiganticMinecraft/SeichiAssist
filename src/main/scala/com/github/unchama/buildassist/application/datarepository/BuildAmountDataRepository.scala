package com.github.unchama.buildassist.application.datarepository

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, IO, SyncEffect, Timer}
import com.github.unchama.buildassist.application.routine.BuildLevelSynchronizationRoutine
import com.github.unchama.buildassist.domain.actions.LevelUpNotifier
import com.github.unchama.buildassist.domain.playerdata.{BuildAmountData, BuildAmountDataPersistence}
import com.github.unchama.datarepository.bukkit.player.TwoPhasedPlayerDataRepository
import com.github.unchama.generic.{ContextCoercion, Diff, WeakRef}
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player

import java.util.UUID

class BuildAmountDataRepository[
  F[_] : SyncEffect : LevelUpNotifier[*[_], Player],
  G[_] : ConcurrentEffect : Timer : ErrorLogger : ContextCoercion[F, *[_]]
](implicit persistence: BuildAmountDataPersistence[F])
  extends TwoPhasedPlayerDataRepository[F, Ref[F, BuildAmountData]] {

  import cats.effect.implicits._
  import cats.implicits._

  override protected type TemporaryData = BuildAmountData

  override protected val loadTemporaryData: (String, UUID) => F[Either[Option[String], BuildAmountData]] =
    (_, uuid) =>
      persistence
        .read(uuid)
        .map(_.getOrElse(BuildAmountData.initial))
        .map(Right.apply)

  override protected def initializeValue(player: Player, temporaryData: BuildAmountData): F[Ref[F, BuildAmountData]] = {
    val updatedData = temporaryData.withSyncedLevel
    val levelDiffOption = Diff.fromValues(temporaryData.desyncedLevel, updatedData.desyncedLevel)

    val notifyLevelDiff = levelDiffOption.foldMapA(LevelUpNotifier[F, Player].notifyTo(player))

    for {
      _ <- notifyLevelDiff
      dataRef <- Ref.in[F, G, BuildAmountData](updatedData)

      weakDataRef = WeakRef.of[G, Ref[G, BuildAmountData]](dataRef)
      _ <-
        BuildLevelSynchronizationRoutine(player, weakDataRef)
          .start
          .runAsync(_ => IO.unit)
          .runSync[F]
    } yield dataRef
  }

  override protected val finalizeBeforeUnload: (Player, Ref[F, BuildAmountData]) => F[Unit] =
    (player, dataRef) => dataRef.get.flatMap(persistence.write(player.getUniqueId, _))
}
