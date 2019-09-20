package com.github.unchama.seichiassist

import com.github.unchama.seichiassist.data.Coordinate
import com.github.unchama.seichiassist.effect.XYZTuple
import com.github.unchama.seichiassist.effect.arrow.ArrowEffects
import kotlin.DeprecationLevel
import kotlinx.coroutines.GlobalScope
import org.bukkit.ChatColor._
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Location, Material}

enum class ActiveSkillPremiumEffect(val num: Int, private val sql_name: String, val desc: String, val explain: String, val usePoint: Int, val material: Material) {
  MAGIC(1, "ef_magic", RED.toString() + "" + UNDERLINE + "" + BOLD + "マジック", "鶏が出る手品", 10, Material.RED_ROSE);

  internal var plugin = SeichiAssist.instance

  def getsqlName(): String = {
    return this.sql_name
  }

  //エフェクトの実行処理分岐 範囲破壊と複数範囲破壊

  @Deprecated(message = "Coordinate deprecation", level = DeprecationLevel.WARNING,
      replaceWith = ReplaceWith("runBreakEffect(player, tool, breaklist, start.toXYZTuple(), end.toXYZTuple(), standard)",
          "com.github.unchama.seichiassist.effect.toXYZTuple",
          "com.github.unchama.seichiassist.effect.toXYZTuple")
  )
  def runBreakEffect(player: Player, tool: ItemStack, breaklist: Set[Block], start: Coordinate, end: Coordinate, standard: Location) {
    runBreakEffect(player, tool, breaklist, start.toXYZTuple(), end.toXYZTuple(), standard)
  }

  def runBreakEffect(player: Player, tool: ItemStack, breaklist: Set[Block], start: XYZTuple, end: XYZTuple, standard: Location) {
    when (this) {
      MAGIC => if (SeichiAssist.DEBUG) {
        MagicTask(player, tool, breaklist, start, end, standard).runTaskTimer(plugin, 0, 100)
      } else {
        MagicTask(player, tool, breaklist, start, end, standard).runTaskTimer(plugin, 0, 10)
      }
    }
  }

  //エフェクトの実行処理分岐
  def runArrowEffect(player: Player) {
    val effect = when (this) {
      MAGIC => ArrowEffects.singleArrowMagicEffect
    }

    GlobalScope.launch {
      effect.runFor(player)
    }
  }
}

object ActiveSkillPremiumEffect {
  def fromSqlName(sqlName: String): ActiveSkillPremiumEffect = {
    return values().find { effect => sqlName == effect.sql_name }
  }
}
