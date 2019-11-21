package com.github.unchama.seichiassist.effect.breaking

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.{ActiveSkillData, Coordinate}
import com.github.unchama.seichiassist.effect.XYZTuple.AxisAlignedCuboid
import com.github.unchama.seichiassist.effect.{BlockSearching, XYZTuple}
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
    AxisAlignedCuboid(XYZTuple(start.x, start.y, start.z), XYZTuple(end.x, end.y, end.z)).forEachGridPoint(2) { xyzTuple =>
      //逐一更新が必要な位置
      val effectloc = droploc.clone().add(xyzTuple.x.toDouble, xyzTuple.y.toDouble, xyzTuple.z.toDouble)

      if (BlockSearching.containsBlockAround(effectloc, 1, blocks)) {
        // TODO: Effect.EXPLOSION_HUGE => Particle.EXPLOSION_HUGE
        player.getWorld.playEffect(effectloc, Effect.EXPLOSION_HUGE, 1)
      }
    }

    // [0.8, 1.2)
    val vol = new Random().nextFloat() * 0.4f + 0.8f
    player.getWorld.playSound(centerbreakloc, Sound.ENTITY_WITHER_BREAK_BLOCK, 1.0f, vol)

    val stepflag = skillData.skillnum <= 2

    blocks.foreach { b =>
      BreakUtil.breakBlock(player, b, droploc, tool, stepflag)
      SeichiAssist.allblocklist -= b
    }
  }

  private def relativeAverage(i1: Int, i2: Int): Double = ((i1 + i2) / 2).toDouble
}

