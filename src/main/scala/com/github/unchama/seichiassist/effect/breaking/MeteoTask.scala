package com.github.unchama.seichiassist.effect.breaking

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.ActiveSkillData
import com.github.unchama.seichiassist.effect.XYZTuple.AxisAlignedCuboid
import com.github.unchama.seichiassist.effect.{PositionSearching, XYZTuple}
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
                 private val start: XYZTuple,
                 private val end: XYZTuple, _droploc: Location) extends BukkitRunnable() {
  //スキルが発動される中心位置
  private val droploc: Location = _droploc.clone()
  //破壊するブロックの中心位置
  private val centerbreakloc: Location =
    this.droploc.add(relativeAverage(start.x, end.x), relativeAverage(start.y, end.y), relativeAverage(start.z, end.z))

  override def run(): Unit = {
    val blockPositions = blocks.map(_.getLocation).map(XYZTuple.of)
    val world = player.getWorld

    AxisAlignedCuboid(start, end).gridPoints(2).foreach { xyzTuple =>
      import com.github.unchama.seichiassist.effect.XYZTupleSyntax._
      val effectloc = XYZTuple.of(droploc).+(xyzTuple)

      if (PositionSearching.containsOneOfPositionsAround(effectloc, 1, blockPositions)) {
        // TODO: Effect.EXPLOSION_HUGE => Particle.EXPLOSION_HUGE
        world.playEffect(effectloc.toLocation(world), Effect.EXPLOSION_HUGE, 1)
      }
    }

    // [0.8, 1.2)
    val vol = new Random().nextFloat() * 0.4f + 0.8f
    world.playSound(centerbreakloc, Sound.ENTITY_WITHER_BREAK_BLOCK, 1.0f, vol)

    BreakUtil.massBreakBlock(player, blocks, droploc, tool, skillData.skillnum <= 2)
    SeichiAssist.managedBlocks --= blocks
  }

  private def relativeAverage(i1: Int, i2: Int): Double = ((i1 + i2) / 2).toDouble
}

