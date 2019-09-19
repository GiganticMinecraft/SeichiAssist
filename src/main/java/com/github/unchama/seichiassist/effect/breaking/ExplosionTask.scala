package com.github.unchama.seichiassist.effect.breaking

import com.github.unchama.seichiassist.effect.XYZTuple
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ExplosionTask(private val player: Player,
                    private val step: Boolean,
                    private val tool: ItemStack,
                    private val blocks: Set<Block>,
                    private val start: XYZTuple,
                    private val end: XYZTuple,
                    private val droploc: Location) : BukkitRunnable() {

  override def run() {
    AxisAlignedCuboid(start, end).forEachGridPoint(2) { (x, y, z) ->
      val explosionLocation = droploc.clone()
      explosionLocation.add(x.toDouble(), y.toDouble(), z.toDouble())

      if (containsBlockAround(explosionLocation, 1, blocks)) {
        player.world.createExplosion(explosionLocation, 0f, false)
      }
    }

    for (block in blocks) {
      BreakUtil.breakBlock(player, block, droploc, tool, step)
      SeichiAssist.allblocklist.remove(block)
    }
  }
}
