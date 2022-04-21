package com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification

import cats.effect.Sync
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.seichiassist.subsystems.buildcount.BuildCountAPI
import com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.application.actions.NotifyLevelUp
import com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.bukkit.actions.BukkitNotifyLevelUp
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player

object System {

  def backgroundProcess[F[_]: Sync: ErrorLogger, G[_], A](
    buildCountReadAPI: BuildCountAPI[F, G, Player]
  ): F[A] = {
    val action: NotifyLevelUp[F, Player] = BukkitNotifyLevelUp[F]
    StreamExtra.compileToRestartingStream("[buildcount.notification]") {
      buildCountReadAPI.buildLevelUpdates.evalMap {
        case ((player, levelDiff)) =>
          action.ofBuildLevelTo(player)(levelDiff)
      }
    }
  }

}
