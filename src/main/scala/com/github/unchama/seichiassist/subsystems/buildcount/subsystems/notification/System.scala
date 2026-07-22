package com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification

import cats.effect.Async
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.actions.{GetConnectedPlayers, OnMinecraftServerThread}
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
import org.typelevel.log4cats.ErrorLogger
import org.bukkit.entity.Player

object System {

  def backgroundProcess[F[_]: Async: ErrorLogger, G[_], A](
    buildCountReadAPI: BuildCountAPI[F, G, Player]
  )(
    implicit getConnectedPlayers: GetConnectedPlayers[F, Player],
    onMinecraftServerThread: OnMinecraftServerThread[F],
    discordNotificationAPI: DiscordNotificationAPI[F]
  ): F[A] = {
    val notifyLevelUp: NotifyLevelUp[F, Player] = BukkitNotifyLevelUp[F](
      using Async[F],
      onMinecraftServerThread,
      discordNotificationAPI,
      getConnectedPlayers
    )
    val notifyBuildAmountThreshold: NotifyBuildAmountThreshold[F, Player] =
      BukkitNotifyBuildAmountThreshold[F](
        using Async[F],
        discordNotificationAPI,
        onMinecraftServerThread,
        getConnectedPlayers
      )
    StreamExtra.compileToRestartingStream("[buildcount.notification]") {
      val levelNotification =
        buildCountReadAPI.buildLevelUpdates.evalMap {
          case (player, levelDiff) =>
            notifyLevelUp.ofBuildLevelTo(player)(levelDiff)
        }

      val amountThresholdNotification =
        buildCountReadAPI.buildAmountUpdateDiffs.evalMap {
          case (player, amountDiff) =>
            notifyBuildAmountThreshold.ofBuildAmountTo(player)(amountDiff)
        }

      levelNotification.merge(amountThresholdNotification)
    }
  }

}
