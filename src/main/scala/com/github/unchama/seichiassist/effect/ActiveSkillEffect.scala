package com.github.unchama.seichiassist.effect

import com.github.unchama.seichiassist.data.{ActiveSkillData, XYZTuple}
import com.github.unchama.seichiassist.util.BreakUtil
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

trait ActiveSkillEffect {
  def runBreakEffect(player: Player,
                     skillData: ActiveSkillData,
                     tool: ItemStack,
                     breakBlocks: Set[Block],
                     start: XYZTuple,
                     end: XYZTuple,
                     standard: Location): Unit
}

object ActiveSkillEffect {
  object NoEffect extends ActiveSkillEffect {
    override def runBreakEffect(player: Player,
                                skillData: ActiveSkillData,
                                tool: ItemStack,
                                breakBlocks: Set[Block],
                                start: XYZTuple,
                                end: XYZTuple,
                                standard: Location): Unit = {
      breakBlocks.foreach { b =>
        BreakUtil.breakBlock(player, b, player.getLocation, tool, shouldPlayBreakSound = false)
      }
    }
  }
}