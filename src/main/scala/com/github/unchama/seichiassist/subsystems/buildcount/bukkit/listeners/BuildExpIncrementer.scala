package com.github.unchama.seichiassist.subsystems.buildcount.bukkit.listeners

import com.github.unchama.runSync

import cats.effect.SyncIO
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.buildcount.application.actions.IncrementBuildExpWhenBuiltByHand
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.{EventHandler, Listener}

/**
 * Created by karayuu on 2020/10/07
 */
class BuildExpIncrementer[F[_]: [f[_]] =>> IncrementBuildExpWhenBuiltByHand[f, Player]: [f[
  _
]] =>> ContextCoercion[f, SyncIO]]
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
