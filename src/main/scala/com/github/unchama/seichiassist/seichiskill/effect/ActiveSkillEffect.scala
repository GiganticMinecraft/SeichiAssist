package com.github.unchama.seichiassist.seichiskill.effect

import cats.effect.IO
import com.github.unchama.seichiassist.MaterialSets.{BlockBreakableBySkill, BreakTool}
import com.github.unchama.seichiassist.data.AxisAlignedCuboid
import com.github.unchama.seichiassist.seichiskill.ActiveSkill
import com.github.unchama.seichiassist.seichiskill.effect.arrow.ArrowEffects
import com.github.unchama.seichiassist.util.BreakUtil
import com.github.unchama.targetedeffect.TargetedEffect
import enumeratum.EnumEntry.Snakecase
import org.bukkit.entity.Player
import org.bukkit.{Location, Material}

sealed trait ActiveSkillEffect {
  val nameOnUI: String

  val arrowEffect: TargetedEffect[Player]

  def runBreakEffect(player: Player,
                     usedSkill: ActiveSkill,
                     tool: BreakTool,
                     breakBlocks: Set[BlockBreakableBySkill],
                     breakArea: AxisAlignedCuboid,
                     standard: Location): IO[Unit]
}

trait SerializableActiveSkillEffect extends ActiveSkillEffect with Snakecase

object ActiveSkillEffect {
  object NoEffect extends ActiveSkillEffect {
    override val nameOnUI: String = "未設定"

    override val arrowEffect: TargetedEffect[Player] = ArrowEffects.normalArrowEffect

    override def runBreakEffect(player: Player,
                                usedSkill: ActiveSkill,
                                tool: BreakTool,
                                breakBlocks: Set[BlockBreakableBySkill],
                                breakArea: AxisAlignedCuboid,
                                standard: Location): IO[Unit] =
      BreakUtil.massBreakBlock(player, breakBlocks, player.getLocation, tool, shouldPlayBreakSound = false, Material.AIR)
  }
}