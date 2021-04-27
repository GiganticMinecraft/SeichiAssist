package com.github.unchama.seichiassist.subsystems.breakcount.subsystems.notification

import cats.effect.Concurrent
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.breakcount.subsystems.notification.application.actions.NotifyLevelUp
import com.github.unchama.seichiassist.subsystems.breakcount.subsystems.notification.bukkit.actions.BukkitNotifyLevelUp
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player

object System {

  def backgroundProcess[
    F[_] : Concurrent : OnMinecraftServerThread : ErrorLogger,
    G[_],
    A
  ](breakCountReadAPI: BreakCountReadAPI[F, G, Player]): F[A] = {
    val action: NotifyLevelUp[F, Player] = BukkitNotifyLevelUp[F]

    StreamExtra.compileToRestartingStream {
      breakCountReadAPI
        .seichiLevelUpdates
        .either(breakCountReadAPI.seichiStarLevelUpdates)
        .evalMap {
          case Left((player, levelDiff)) =>
            action.ofSeichiLevelTo(player)(levelDiff)
          case Right((player, levelDiff)) =>
            action.ofSeichiStarLevelTo(player)(levelDiff)
        }
    }
  }

}
