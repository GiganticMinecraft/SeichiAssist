package com.github.unchama.seichiassist.effect

import com.github.unchama.seichiassist.data.{ActiveSkillData, XYZTuple}
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

trait ActiveSkillEffect {
  def runBreakEffect(player: Player,
                     skillData: ActiveSkillData,
                     tool: ItemStack,
                     breakList: Set[Block],
                     start: XYZTuple,
                     end: XYZTuple,
                     standard: Location): Unit
}
