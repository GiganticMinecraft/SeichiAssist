package com.github.unchama.seichiassist.subsystems.disablegrowth.bukkit.listeners

import org.bukkit.Material
import org.bukkit.event.block.BlockGrowEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.util.Vector;

class DisableGrowthListener extends Listener {
  @EventHandler
  def onCactusGrow(event: BlockGrowEvent): Unit = {
    // BlockGrowEvent#getBlockで得られるのは、成長するサボテンが出現する座標のブロック（つまり、Material.AIR）になるので、Y座標を-1した座標のブロックのMaterialを参照する
    val location = event.getBlock.getLocation.subtract(new Vector(0, -1, 0))
    if (location.getBlock.getType == Material.CACTUS) {
      event.setCancelled(true)
    }
  }
}
