package com.github.unchama.seichiassist.subsystems.breakcount.bukkit.actions

import cats.effect.Sync
import com.github.unchama.seichiassist.ManagedWorld
import com.github.unchama.seichiassist.subsystems.breakcount.application.actions.ClassifyPlayerWorld
import org.bukkit.entity.Player

object SyncClassifyBukkitPlayerWorld {

  import ManagedWorld._

  def apply[F[_]: Sync]: ClassifyPlayerWorld[F, Player] =
    (player: Player) => Sync[F].delay(player.getWorld.isSeichi)

}
