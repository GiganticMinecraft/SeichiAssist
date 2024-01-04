package com.github.unchama.seichiassist.subsystems.blockliquidstream.bukkit

import org.bukkit.block.data.Waterlogged
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.block.Block
import com.github.unchama.seichiassist.ManagedWorld._

class LiquidStreamListener extends Listener {

  @EventHandler
  def onBlockMove(event: BlockFromToEvent): Unit = {
    event.setCancelled(isLiquidStream(event.getBlock))
  }

  private def isLiquidStream(block: Block): Boolean = {
    val world = block.getWorld

    if (world.isNotSeichi) return false
    val blockData = block.getBlockData

    blockData match {
      case waterlogged: Waterlogged if !block.isLiquid && !waterlogged.isWaterlogged => false
      case _                                                                         => true
    }
  }

}
