package com.github.unchama.seichiassist.subsystems.breakcount.subsystems.notification

import cats.effect.{Async, Concurrent}
import com.github.unchama.minecraft.actions.MinecraftServerThreadShift
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.breakcount.subsystems.notification.application.actions.NotifyLevelUp
import com.github.unchama.seichiassist.subsystems.breakcount.subsystems.notification.bukkit.actions.SyncBukkitNotifyLevelUp
import org.bukkit.entity.Player

object System {

  import cats.implicits._

  def backgroundProcess[
    F[_] : Concurrent : MinecraftServerThreadShift,
    G[_],
    A
  ](breakCountReadAPI: BreakCountReadAPI[F, G, Player]): F[A] = {
    val action: NotifyLevelUp[F, Player] = SyncBukkitNotifyLevelUp[F]

    breakCountReadAPI
      .seichiLevelUpdates
      .either(breakCountReadAPI.seichiStarLevelUpdates)
      .evalMap {
        case Left((player, levelDiff)) =>
          action.ofSeichiLevelTo(player)(levelDiff)
        case Right((player, levelDiff)) =>
          action.ofSeichiStarLevelTo(player)(levelDiff)
      }
      .compile
      .drain
      .flatMap(_ => Async[F].never)
  }

}
