package com.github.unchama.buildassist.infrastructure

import cats.Monad
import cats.effect.SyncEffect
import cats.effect.concurrent.Ref
import com.github.unchama.buildassist.domain.actions.LevelUpNotification
import com.github.unchama.buildassist.domain.playerdata.{BuildAmountData, BuildAmountDataPersistence}
import com.github.unchama.datarepository.bukkit.player.TwoPhasedPlayerDataRepository
import com.github.unchama.util.Diff
import org.bukkit.entity.Player

import java.util.UUID

class BuildAmountDataRepository[
  F[_] : SyncEffect : LevelUpNotification[*[_], Player]
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

  override protected def initializeValue(player: Player, temporaryData: BuildAmountData): F[Ref[F, BuildAmountData]] = {
    val updatedData = temporaryData.withSyncedLevel
    val levelDiffOption = Diff.fromValues(temporaryData.desyncedLevel, updatedData.desyncedLevel)

    val notifyLevelDiff = levelDiffOption.fold(Monad[F].unit)(LevelUpNotification[F, Player].notifyTo(player))

    notifyLevelDiff >> Ref.of(updatedData)
  }

  override protected val finalizeBeforeUnload: (Player, Ref[F, BuildAmountData]) => F[Unit] =
    (player, dataRef) => dataRef.get.flatMap(persistence.write(player.getUniqueId, _))
}
