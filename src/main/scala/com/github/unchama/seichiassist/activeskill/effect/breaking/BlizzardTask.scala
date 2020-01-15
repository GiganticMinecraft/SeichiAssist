package com.github.unchama.seichiassist.activeskill.effect.breaking

import com.github.unchama.seichiassist.data.ActiveSkillData
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
  //1回目のrun
  override def firstAction(): Unit = {
    if (skillData.skillnum > 2) {
      com.github.unchama.seichiassist.unsafe.runIOAsync(
        "ブロックを大量破壊する",
        BreakUtil.massBreakBlock(player, blocks, droploc, tool, shouldPlayBreakSound = false, Material.PACKED_ICE)
      )
    } else {
      com.github.unchama.seichiassist.unsafe.runIOAsync(
        "ブロックを大量破壊する",
        BreakUtil.massBreakBlock(player, blocks, droploc, tool, shouldPlayBreakSound = true)
      )
      SeichiAssist.managedBlocks --= blocks
      cancel()
    }
  }

  //2回目のrun
  override def secondAction(): Unit = {
    blocks
      .map(_.getLocation)
      .foreach(location => player.getWorld.playEffect(location, Effect.SNOWBALL_BREAK, 1))

    if (skillData.skillnum > 2) {
      blocks.foreach { b =>
        b.setType(Material.AIR)

        if (skillData.skilltype == ActiveSkill.BREAK.gettypenum())
          b.getWorld.playEffect(b.getLocation, Effect.STEP_SOUND, Material.PACKED_ICE, 5)
        else
          b.getWorld.playEffect(b.getLocation, Effect.STEP_SOUND, Material.PACKED_ICE)

        SeichiAssist.managedBlocks -= b
      }
    }
  }
}

