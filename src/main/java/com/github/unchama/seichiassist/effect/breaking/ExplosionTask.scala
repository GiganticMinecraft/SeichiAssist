package com.github.unchama.seichiassist.effect.breaking

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.effect.XYZTuple.AxisAlignedCuboid
import com.github.unchama.seichiassist.effect.{BlockSearching, XYZTuple}
import com.github.unchama.seichiassist.util.BreakUtil
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

class ExplosionTask(private val player: Player,
                    private val step: Boolean,
                    private val tool: ItemStack,
                    private val blocks: Set[Block],
                    private val start: XYZTuple,
                    private val end: XYZTuple,
                    private val droploc: Location) extends BukkitRunnable() {

  override def run() {
    AxisAlignedCuboid(start, end).forEachGridPoint(2) { case XYZTuple(x, y, z) =>
      val explosionLocation = droploc.clone()
      explosionLocation.add(x.toDouble, y.toDouble, z.toDouble)

      if (BlockSearching.containsBlockAround(explosionLocation, 1, blocks)) {
        player.getWorld.createExplosion(explosionLocation, 0f, false)
      }
    }

    blocks.foreach { block =>
      BreakUtil.breakBlock(player, block, droploc, tool, step)
      SeichiAssist.allblocklist -= block
    }
  }
}
