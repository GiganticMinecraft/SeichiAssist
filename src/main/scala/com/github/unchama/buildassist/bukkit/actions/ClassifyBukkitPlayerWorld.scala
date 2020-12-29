package com.github.unchama.buildassist.bukkit.actions

import cats.effect.Sync
import com.github.unchama.buildassist.application.actions.ClassifyPlayerWorld
import org.bukkit.entity.Player

class ClassifyBukkitPlayerWorld[F[_] : Sync] extends ClassifyPlayerWorld[F, Player] {

  import com.github.unchama.seichiassist.ManagedWorld._

  override def isInSeichiWorld(player: Player): F[Boolean] =
    Sync[F].delay {
      player.getWorld.isSeichi
    }

  override def isInBuildWorld(player: Player): F[Boolean] =
    Sync[F].delay {
      player.getWorld.shouldTrackBuildBlock
    }
}
