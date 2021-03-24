package com.github.unchama.seichiassist.subsystems.mana

import cats.effect.{Async, Sync}
import com.github.unchama.generic.Diff
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player

object System {

  def backgroundProcess[
    F[_] : Async : ErrorLogger, G[_]
  ](implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player]): F[Nothing] = {
    StreamExtra.compileToRestartingStream {
      breakCountReadAPI
        .seichiLevelUpdates
        .evalTap { case (player, Diff(_, newLevel)) =>
          Sync[F].delay {
            // TODO: manaのリポジトリをこのsubsystemで持ってplayermapを参照しないようにする
            SeichiAssist.playermap.get(player.getUniqueId).foreach(_.manaState.onLevelUp(player, newLevel))
          }
        }
    }
  }

}
