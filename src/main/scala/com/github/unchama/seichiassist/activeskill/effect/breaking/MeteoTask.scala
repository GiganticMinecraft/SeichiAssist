package com.github.unchama.seichiassist.activeskill.effect.breaking

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.activeskill.effect.PositionSearching
import com.github.unchama.seichiassist.data.{ActiveSkillData, AxisAlignedCuboid, XYZTuple}
import com.github.unchama.seichiassist.util.BreakUtil
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.{Effect, Location, Sound}

import scala.util.Random

class MeteoTask(
                 private val player: Player,
                 private val skillData: ActiveSkillData,
                 private val tool: ItemStack,
                 private val blocks: Set[Block],
                 breakArea: AxisAlignedCuboid,
                 _dropLoc: Location) extends BukkitRunnable() {

  import com.github.unchama.seichiassist.data.syntax._

  //スキルが発動される中心位置
  private val itemDropLoc: Location = _dropLoc.clone()

  //破壊するブロックの中心位置
  private val centerBreakLoc: Location = this.itemDropLoc + ((breakArea.begin + breakArea.end) / 2)

  override def run(): Unit = {
    val blockPositions = blocks.map(_.getLocation).map(XYZTuple.of)
    val world = player.getWorld

    import com.github.unchama.seichiassist.data.syntax._
    breakArea.gridPoints(2).foreach { xyzTuple =>
      val effectLoc = XYZTuple.of(itemDropLoc).+(xyzTuple)

      if (PositionSearching.containsOneOfPositionsAround(effectLoc, 1, blockPositions)) {
        // TODO: Effect.EXPLOSION_HUGE => Particle.EXPLOSION_HUGE
        world.playEffect(effectLoc.toLocation(world), Effect.EXPLOSION_HUGE, 1)
      }
    }

    // [0.8, 1.2)
    val vol = new Random().nextFloat() * 0.4f + 0.8f
    world.playSound(centerBreakLoc, Sound.ENTITY_WITHER_BREAK_BLOCK, 1.0f, vol)

    com.github.unchama.seichiassist.unsafe.runIOAsync(
      "ブロックを大量破壊する",
      BreakUtil.massBreakBlock(player, blocks, itemDropLoc, tool, skillData.skillnum <= 2)
    )
    SeichiAssist.managedBlocks --= blocks
  }
}

