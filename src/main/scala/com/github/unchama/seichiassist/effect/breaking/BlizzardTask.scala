package com.github.unchama.seichiassist.effect.breaking

import com.github.unchama.seichiassist.data.ActiveSkillData
import com.github.unchama.seichiassist.effect.XYZTuple
import com.github.unchama.seichiassist.effect.XYZTuple.AxisAlignedCuboid
import com.github.unchama.seichiassist.util.BreakUtil
import com.github.unchama.seichiassist.{ActiveSkill, SeichiAssist}
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Effect, Location, Material}

class BlizzardTask(private val player: Player,
                   private val skillData: ActiveSkillData,
                   private val tool: ItemStack,
                   private val blocks: Set[Block],
                   private val droploc: Location) extends RoundedTask() {
  //音の聞こえる距離
  private var soundRadius: Int = 0
  private var setRadius: Boolean = false

  //1回目のrun
  override def firstAction(): Unit = {
    if (skillData.skillnum > 2) {
      blocks.foreach { block =>
        BreakUtil.breakBlock(player, block, droploc, tool, stepflag = false)
        block.setType(Material.PACKED_ICE)
      }
    } else {
      blocks.foreach { block =>
        BreakUtil.breakBlock(player, block, droploc, tool, stepflag = true)
        SeichiAssist.allblocklist -= block
      }
      cancel()
    }

    soundRadius = 5
    setRadius = skillData.skilltype == ActiveSkill.BREAK.gettypenum()
  }

  //2回目のrun
  override def secondAction(): Unit = {
    blocks
      .map(_.getLocation)
      .foreach(location => player.getWorld.playEffect(location, Effect.SNOWBALL_BREAK, 1))

    if (skillData.skillnum > 2) {
      blocks.foreach { b =>
        b.setType(Material.AIR)

        if (setRadius)
          b.getWorld.playEffect(b.getLocation, Effect.STEP_SOUND, Material.PACKED_ICE, soundRadius)
        else
          b.getWorld.playEffect(b.getLocation, Effect.STEP_SOUND, Material.PACKED_ICE)

        SeichiAssist.allblocklist -= b
      }
    }
  }
}

