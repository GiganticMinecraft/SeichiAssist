package com.github.unchama.seichiassist.subsystems.fastdiggingeffect

import cats.effect.{Async, Concurrent, Sync, Timer}
import com.github.unchama.seichiassist.data.potioneffect.FastDiggingEffect
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.{Config, SeichiAssist}
import org.bukkit.entity.Player

import scala.concurrent.duration.DurationInt

object System {

  import cats.implicits._
  import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid._

  // TODO configを丸々受け取らずシステムのコンフィグを用意する
  def backgroundProcess[
    F[_]
    : Timer
    : Concurrent, G[_]
  ](config: Config)(implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player]): F[Nothing] = {
    breakCountReadAPI
      .batchedIncreases(1.minute)
      .evalTap(batch =>
        Sync[F].delay {
          batch.toUuidCollatedList.foreach { case (player, amount) =>
            val effect = new FastDiggingEffect(amount.amount.toDouble * config.getMinuteMineSpeed, 2)

            // TODO: FastDiggingEffectのリポジトリをこのsubsystemで持って、playermapを参照しないようにする
            SeichiAssist.playermap.get(player.getUniqueId).foreach(_.effectdatalist.addOne(effect))
          }
        }
      )
      .compile.drain
      .flatMap[Nothing](_ => Async[F].never)
  }

}
