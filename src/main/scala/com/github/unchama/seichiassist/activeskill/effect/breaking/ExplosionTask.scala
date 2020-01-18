package com.github.unchama.seichiassist.activeskill.effect.breaking

import cats.effect.IO
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.activeskill.effect.PositionSearching
import com.github.unchama.seichiassist.data.{AxisAlignedCuboid, XYZTuple}
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
                    breakArea: AxisAlignedCuboid,
                    private val dropLoc: Location) extends BukkitRunnable() {
  override def run(): Unit = {
    SeichiAssist.managedBlocks --= blocks

    val blockPositions = blocks.map(_.getLocation).map(XYZTuple.of)
    val world = player.getWorld

    import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.asyncShift
    import com.github.unchama.seichiassist.data.syntax._

    com.github.unchama.seichiassist.unsafe.runIOAsync(
      "エクスプロージョンの効果を発生させる",
      for {
        _ <- asyncShift.shift

        explosionLocations <- IO {
          breakArea
            .gridPoints(2)
            .map(XYZTuple.of(dropLoc) + _)
            .filter(PositionSearching.containsOneOfPositionsAround(_, 1, blockPositions))
        }

        _ <- BreakUtil.massBreakBlock(player, blocks, dropLoc, tool, step)

        _ <- IO {
          explosionLocations.foreach(coordinates =>
            world.createExplosion(coordinates.toLocation(world), 0f, false)
          )
        }
      } yield ()
    )
  }
}
