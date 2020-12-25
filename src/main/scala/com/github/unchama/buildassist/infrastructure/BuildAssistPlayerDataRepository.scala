package com.github.unchama.buildassist.infrastructure

import cats.Monad
import cats.effect.SyncEffect
import cats.effect.concurrent.Ref
import com.github.unchama.buildassist.domain.actions.LevelUpNotification
import com.github.unchama.buildassist.domain.playerdata.{BuildAssistPlayerData, BuildAssistPlayerDataPersistence}
import com.github.unchama.datarepository.bukkit.player.TwoPhasedPlayerDataRepository
import com.github.unchama.util.Diff
import org.bukkit.entity.Player

import java.util.UUID

class BuildAssistPlayerDataRepository[
  F[_] : SyncEffect : LevelUpNotification[*[_], Player]
](implicit persistence: BuildAssistPlayerDataPersistence[F])
  extends TwoPhasedPlayerDataRepository[F, Ref[F, BuildAssistPlayerData]] {

  import cats.implicits._

  override protected type TemporaryData = BuildAssistPlayerData

  override protected val loadTemporaryData: (String, UUID) => F[Either[Option[String], BuildAssistPlayerData]] =
    (_, uuid) =>
      persistence
        .read(uuid)
        .map(_.getOrElse(BuildAssistPlayerData.initialData))
        .map(Right.apply)

  override protected def initializeValue(player: Player, temporaryData: BuildAssistPlayerData): F[Ref[F, BuildAssistPlayerData]] = {
    val updatedData = temporaryData.withSyncedLevel
    val levelDiffOption = Diff.fromValues(temporaryData.desyncedLevel, updatedData.desyncedLevel)

    val notifyLevelDiff = levelDiffOption.fold(Monad[F].unit)(LevelUpNotification[F, Player].notifyTo(player))

    notifyLevelDiff >> Ref.of(updatedData)
  }

  override protected val finalizeBeforeUnload: (Player, Ref[F, BuildAssistPlayerData]) => F[Unit] =
    (player, dataRef) => dataRef.get.flatMap(persistence.write(player.getUniqueId, _))
}
