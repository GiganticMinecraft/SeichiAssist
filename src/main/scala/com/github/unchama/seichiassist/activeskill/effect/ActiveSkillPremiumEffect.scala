package com.github.unchama.seichiassist.activeskill.effect

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.activeskill.effect.arrow.ArrowEffects
import com.github.unchama.seichiassist.activeskill.effect.breaking.MagicTask
import com.github.unchama.seichiassist.data.{ActiveSkillData, AxisAlignedCuboid}
import com.github.unchama.targetedeffect.TargetedEffect
import enumeratum._
import org.bukkit.ChatColor._
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Location, Material}

sealed abstract class ActiveSkillPremiumEffect(val num: Int,
                                               val sql_name: String,
                                               val desc: String,
                                               val explain: String,
                                               val usePoint: Int,
                                               val material: Material) extends EnumEntry with ActiveSkillEffect {
  @Deprecated
  def getsqlName: String = this.sql_name

  def runBreakEffect(player: Player,
                     skillData: ActiveSkillData,
                     tool: ItemStack,
                     breakBlocks: Set[Block],
                     breakArea: AxisAlignedCuboid,
                     standard: Location): Unit = {
    this match {
      case ActiveSkillPremiumEffect.MAGIC => if (SeichiAssist.DEBUG) {
        new MagicTask(player, tool, breakBlocks, breakArea, standard).runTaskTimer(SeichiAssist.instance, 0, 100)
      } else {
        new MagicTask(player, tool, breakBlocks, breakArea, standard).runTaskTimer(SeichiAssist.instance, 0, 10)
      }
    }
  }

  //エフェクトの実行処理分岐
  def arrowEffect(player: Player): TargetedEffect[Player] =
    this match {
      case ActiveSkillPremiumEffect.MAGIC => ArrowEffects.singleArrowMagicEffect
    }
}

case object ActiveSkillPremiumEffect extends Enum[ActiveSkillPremiumEffect] {

  val values: IndexedSeq[ActiveSkillPremiumEffect] = findValues
  /**
   * @deprecated for interop purpose only
   */
  @Deprecated()
  val arrayValues: Array[ActiveSkillPremiumEffect] = values.toArray

  def fromSqlName(sqlName: String): Option[ActiveSkillPremiumEffect] = values.find(sqlName == _.sql_name)

  case object MAGIC extends ActiveSkillPremiumEffect(1, "ef_magic", s"$RED$UNDERLINE${BOLD}マジック", "鶏が出る手品", 10, Material.RED_ROSE)
}
