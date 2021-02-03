package com.github.unchama.seichiassist.subsystems.gachapoint

import cats.effect.{Async, Sync}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import org.bukkit.entity.Player

object System {

  import cats.implicits._

  def backgroundProcess[
    F[_] : Async, G[_]
  ](implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player]): F[Nothing] = {
    breakCountReadAPI
      .seichiAmountIncreases
      .evalTap { case (player, amount) =>
        Sync[F].delay {
          // TODO: gachapointのリポジトリをこのsubsystemで持ってplayermapを参照しないようにする
          SeichiAssist.playermap.get(player.getUniqueId).foreach(_.gachapoint += amount.amount.toInt)
        }
      }
      .compile.drain
      .flatMap[Nothing](_ => Async[F].never)
  }

}
