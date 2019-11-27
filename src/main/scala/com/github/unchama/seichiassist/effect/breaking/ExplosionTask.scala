package com.github.unchama.seichiassist.effect.breaking

import cats.effect.IO
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.{AxisAlignedCuboid, XYZTuple}
import com.github.unchama.seichiassist.effect.PositionSearching
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
                    private val dropLoc: Location) extends BukkitRunnable() {
  override def run(): Unit = {
    SeichiAssist.managedBlocks --= blocks

    BreakUtil.massBreakBlock(player, blocks, dropLoc, tool, step)

    val blockPositions = blocks.map(_.getLocation).map(XYZTuple.of)
    val world = player.getWorld

    import com.github.unchama.seichiassist.data.syntax._
    import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.asyncShift

    com.github.unchama.seichiassist.unsafe.fireShiftAndRunAsync(
      "爆発エフェクトを再生する",
      IO {
        AxisAlignedCuboid(start, end).gridPoints(2).foreach { gridPoint =>
          val explosionLocation = XYZTuple.of(dropLoc) + gridPoint

          if (PositionSearching.containsOneOfPositionsAround(XYZTuple.of(dropLoc) + gridPoint, 1, blockPositions)) {
            world.createExplosion(explosionLocation.toLocation(world), 0f, false)
          }
        }
      }
    )
  }
}
