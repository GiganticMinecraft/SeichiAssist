package com.github.unchama.seichiassist.subsystems.buildcount.bukkit.listeners

import cats.effect.SyncEffect.ops.toAllSyncEffectOps
import cats.effect.{SyncEffect, SyncIO}
import com.github.unchama.seichiassist.subsystems.buildcount.application.actions.IncrementBuildExpWhenBuiltByHand
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.{EventHandler, Listener}

/**
 * Created by karayuu on 2020/10/07
 */
class BuildExpIncrementer[F[_]: IncrementBuildExpWhenBuiltByHand[*[_], Player]: SyncEffect]
    extends Listener {

  @EventHandler(ignoreCancelled = true)
  def onEvent(event: BlockPlaceEvent): Unit = {
    if (event.getBlockPlaced.getType.isSolid) {
      IncrementBuildExpWhenBuiltByHand[F, Player]
        .of(event.getPlayer)
        .runSync[SyncIO]
        .unsafeRunSync()
    }
  }
}
