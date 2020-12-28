package com.github.unchama.buildassist.application.routine

import cats.effect.Timer
import cats.effect.concurrent.Ref
import cats.{Applicative, MonadError}
import com.github.unchama.buildassist.application.actions.LevelUpNotifier
import com.github.unchama.buildassist.domain.playerdata.BuildAmountData
import com.github.unchama.concurrent.RepeatingRoutine
import com.github.unchama.generic.{Diff, WeakRef}
import com.github.unchama.minecraft.actions.SendMinecraftMessage
import io.chrisdavenport.log4cats.ErrorLogger

object BuildLevelSynchronizationRoutine {

  def apply[
    Player,
    F[_]
    : MonadError[*[_], Throwable]
    : Timer
    : SendMinecraftMessage[*[_], Player]
    : ErrorLogger
  ](player: Player, updateTargetRef: WeakRef[F, Ref[F, BuildAmountData]]): F[Unit] = {
    import cats.implicits._

    import scala.concurrent.duration._

    val getRepeatInterval: F[FiniteDuration] = Applicative[F].pure(1.minute)
    val routineAction: Ref[F, BuildAmountData] => F[Unit] = ref =>
      for {
        dataPair <- ref.modify { data =>
          (data.withSyncedLevel, (data, data.withSyncedLevel))
        }
        (oldData, updatedData) = dataPair
        _ <-
          Diff
            .fromValues(oldData.desyncedLevel, updatedData.desyncedLevel)
            .foldMapA(LevelUpNotifier[F, Player].notifyTo(player))
      } yield ()

    RepeatingRoutine.whileReferencedRecovering(updateTargetRef, routineAction, getRepeatInterval)
  }

}
