package com.github.unchama.seichiassist.effect.breaking

import com.github.unchama.seichiassist.data.{ActiveSkillData, Coordinate}
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BlizzardTask(private val player: Player, private val skillData: ActiveSkillData,
                   private val tool: ItemStack,
                   private val blocks: Set[Block],
                   private val start: Coordinate,
                   private val end: Coordinate,
                   private val droploc: Location) extends RoundedTask() {
  //音の聞こえる距離
  private var soundRadius: Int = 0
  private var setRadius: Boolean = false

  override def firstAction() {
    //1回目のrun
    if (skillData.skillnum > 2) {
      for (b in blocks) {
        BreakUtil.breakBlock(player, b, droploc, tool, false)
        b.type = Material.PACKED_ICE
      }
    } else {
      for (b in blocks) {
        BreakUtil.breakBlock(player, b, droploc, tool, true)
        SeichiAssist.allblocklist -= b
      }
      cancel()
    }
    soundRadius = 5
    setRadius = skillData.skilltype == ActiveSkill.BREAK.gettypenum()
  }

  override def secondAction() {
    //2回目のrun
    AxisAlignedCuboid(XYZTuple(start.x, start.y, start.z), XYZTuple(end.x, end.y, end.z)).forEachGridPoint { xyzTuple =>
      //逐一更新が必要な位置
      val effectloc = droploc.clone().add(xyzTuple.x.toDouble(), xyzTuple.y.toDouble(), xyzTuple.z.toDouble())
      if (blocks.contains(effectloc.block)) {
        player.world.playEffect(effectloc, Effect.SNOWBALL_BREAK, 1)
      }
    }

    if (skillData.skillnum > 2) {
      for (b in blocks) {
        b.type = Material.AIR
        if (setRadius) {
          b.world.playEffect(b.location, Effect.STEP_SOUND, Material.PACKED_ICE, soundRadius)
        } else {
          b.world.playEffect(b.location, Effect.STEP_SOUND, Material.PACKED_ICE)
        }
        SeichiAssist.allblocklist -= b
      }
    }
  }
}

