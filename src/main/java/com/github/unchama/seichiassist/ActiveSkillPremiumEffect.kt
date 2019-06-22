package com.github.unchama.seichiassist

import com.github.unchama.seichiassist.effect.arrow.ArrowMagicTask
import com.github.unchama.seichiassist.effect.breaking.MagicTask
import com.github.unchama.seichiassist.data.Coordinate
import com.github.unchama.seichiassist.data.PlayerData
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

import java.util.Arrays

enum class ActiveSkillPremiumEffect(val num: Int, private val sql_name: String, val desc: String, val explain: String, val usePoint: Int, val material: Material) {
  MAGIC(1, "ef_magic", ChatColor.RED.toString() + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "マジック", "鶏が出る手品", 10, Material.RED_ROSE);

  internal var plugin = SeichiAssist.instance

  fun getsqlName(): String {
    return this.sql_name
  }

  //プレイヤーが所持しているかどうか
  fun isObtained(flagmap: Map<Int, Boolean>): Boolean? {
    return flagmap[num]
  }

  //獲得させる処理
  fun setObtained(flagmap: MutableMap<Int, Boolean>) {
    flagmap[num] = true
  }

  //エフェクトの実行処理分岐 範囲破壊と複数範囲破壊

  fun runBreakEffect(player: Player, tool: ItemStack, breaklist: List<Block>, start: Coordinate, end: Coordinate, standard: Location) {
    when (this) {
      MAGIC -> if (SeichiAssist.DEBUG) {
        MagicTask(player, tool, breaklist, start, end, standard).runTaskTimer(plugin, 0, 100)
      } else {
        MagicTask(player, tool, breaklist, start, end, standard).runTaskTimer(plugin, 0, 10)
      }
    }
  }

  //エフェクトの実行処理分岐
  fun runArrowEffect(player: Player) {
    when (this) {
      MAGIC -> ArrowMagicTask(player)
    }
  }

  companion object {

    fun fromSqlName(sqlName: String): ActiveSkillPremiumEffect? {
      return Arrays
          .stream(values())
          .filter { effect -> sqlName == effect.sql_name }
          .findFirst()
          .orElse(null)
    }
  }
}
