package com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification

import cats.effect.ConcurrentEffect
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.buildcount.BuildCountAPI
import com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.application.actions.{
  NotifyBuildAmountThreshold,
  NotifyLevelUp
}
import com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.bukkit.actions.{
  BukkitNotifyBuildAmountThreshold,
  BukkitNotifyLevelUp
}
import com.github.unchama.seichiassist.subsystems.discordnotification.DiscordNotificationAPI
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player

object System {

  def backgroundProcess[F[
    _
  ]: ConcurrentEffect: OnMinecraftServerThread: ErrorLogger: DiscordNotificationAPI, G[_], A](
    buildCountReadAPI: BuildCountAPI[F, G, Player]
  ): F[A] = {
    val action: NotifyLevelUp[F, Player] = BukkitNotifyLevelUp[F]
    val actionAmount: NotifyBuildAmountThreshold[F, Player] =
      BukkitNotifyBuildAmountThreshold[F]
    StreamExtra.compileToRestartingStream("[buildcount.notification]") {
      val levelNotification =
        buildCountReadAPI.buildLevelUpdates.evalMap {
          case (player, levelDiff) =>
            action.ofBuildLevelTo(player)(levelDiff)
        }

      val amountThresholdNotification =
        buildCountReadAPI.buildAmountUpdateDiffs.evalMap {
          case (player, amountDiff) =>
            actionAmount.ofBuildAmountTo(player)(amountDiff)
        }

      levelNotification.merge(amountThresholdNotification)
    }
  }

}
