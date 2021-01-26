package com.github.unchama.seichiassist.subsystems.mana

import cats.effect.{Async, Sync}
import com.github.unchama.generic.Diff
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import org.bukkit.entity.Player

object System {

  import cats.implicits._

  def backgroundProcess[
    F[_] : Async, G[_]
  ](implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player]): F[Nothing] = {
    breakCountReadAPI
      .seichiLevelUpdates
      .evalTap { case (player, Diff(_, newLevel)) => Sync[F].delay {
        // TODO: manaのリポジトリをこのsubsystemで持ってplayermapを参照しないようにする
        SeichiAssist.playermap.get(player.getUniqueId).foreach(_.manaState.onLevelUp(player, newLevel))
      }
      }
      .compile.drain
      .flatMap[Nothing](_ => Async[F].never)
  }

}
