package com.github.unchama.seichiassist.seichiskill.effect

import cats.effect.IO
import com.github.unchama.seichiassist.MaterialSets.{BlockBreakableBySkill, BreakTool}
import com.github.unchama.seichiassist.data.AxisAlignedCuboid
import com.github.unchama.seichiassist.seichiskill.ActiveSkill
import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillNormalEffect.Blizzard
import com.github.unchama.seichiassist.util.BreakUtil
import org.bukkit.entity.Player
import org.bukkit.{Location, Material}

trait ActiveSkillEffect {
  val nameOnUI: String

  def runBreakEffect(player: Player,
                     usedSkill: ActiveSkill,
                     tool: BreakTool,
                     breakBlocks: Set[BlockBreakableBySkill],
                     breakArea: AxisAlignedCuboid,
                     standard: Location): IO[Unit]
}

object ActiveSkillEffect {
  object NoEffect extends ActiveSkillEffect {
    override val nameOnUI: String = "未設定"

    override def runBreakEffect(player: Player,
                                usedSkill: ActiveSkill,
                                tool: BreakTool,
                                breakBlocks: Set[BlockBreakableBySkill],
                                breakArea: AxisAlignedCuboid,
                                standard: Location): IO[Unit] =
      BreakUtil.massBreakBlock(player, breakBlocks, player.getLocation, tool, shouldPlayBreakSound = false, Material.AIR)
  }

  // できるならActiveSkillDataにActiveSkillEffectを直接持たせたい
  def fromEffectNum(effectNum: Int, skillNum: Int): ActiveSkillEffect = {
    if (effectNum == 0) {
      NoEffect
    } else if (effectNum <= 100) {
      //通常エフェクトが指定されているときの処理(100以下の番号に割り振る)
      ActiveSkillNormalEffect.values(effectNum - 1) match {
        case Blizzard if skillNum < 3 => NoEffect
        case e => e
      }
    } else {
      //プレミアムエフェクトが指定されているときの処理(100超の番号に割り振る)
      ActiveSkillPremiumEffect.values(effectNum - 100 - 1)
    }
  }
}