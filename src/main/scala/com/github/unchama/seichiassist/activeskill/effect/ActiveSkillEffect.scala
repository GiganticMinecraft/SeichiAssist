package com.github.unchama.seichiassist.activeskill.effect

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.{ActiveSkillData, AxisAlignedCuboid}
import com.github.unchama.seichiassist.util.BreakUtil
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Location, Material}

trait ActiveSkillEffect {
  def runBreakEffect(player: Player,
                     skillData: ActiveSkillData,
                     tool: ItemStack,
                     breakBlocks: Set[Block],
                     breakArea: AxisAlignedCuboid,
                     standard: Location): Unit
}

object ActiveSkillEffect {
  object NoEffect extends ActiveSkillEffect {
    override def runBreakEffect(player: Player,
                                skillData: ActiveSkillData,
                                tool: ItemStack,
                                breakBlocks: Set[Block],
                                breakArea: AxisAlignedCuboid,
                                standard: Location): Unit = {
      com.github.unchama.seichiassist.unsafe.runIOAsync(
        "ブロックを大量破壊する",
        BreakUtil.massBreakBlock(player, breakBlocks, player.getLocation, tool, shouldPlayBreakSound = false, Material.AIR)
      )
      SeichiAssist.managedBlocks --= breakBlocks
    }
  }

  // できるならActiveSkillDataにActiveSkillEffectを直接持たせたい
  def fromEffectNum(effectNum: Int): ActiveSkillEffect = {
    if (effectNum == 0) {
      NoEffect
    } else if (effectNum <= 100) {
      //通常エフェクトが指定されているときの処理(100以下の番号に割り振る)
      ActiveSkillNormalEffect.values(effectNum - 1)
    } else {
      //プレミアムエフェクトが指定されているときの処理(100超の番号に割り振る)
      ActiveSkillPremiumEffect.values(effectNum - 100 - 1)
    }
  }
}