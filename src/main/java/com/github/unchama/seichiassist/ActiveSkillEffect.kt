package com.github.unchama.seichiassist

import com.github.unchama.seichiassist.effect.arrow.ArrowBlizzardTask
import com.github.unchama.seichiassist.effect.arrow.ArrowExplosionTask
import com.github.unchama.seichiassist.effect.arrow.ArrowMeteoTask
import com.github.unchama.seichiassist.effect.breaking.BlizzardTask
import com.github.unchama.seichiassist.effect.breaking.ExplosionTask
import com.github.unchama.seichiassist.effect.breaking.MeteoTask
import com.github.unchama.seichiassist.data.Coordinate
import com.github.unchama.seichiassist.data.PlayerData
import com.okkero.skedule.BukkitSchedulerController
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

import java.util.ArrayList
import java.util.Arrays

enum class ActiveSkillEffect constructor(val num: Int, private val sql_name: String, val desc: String, val explain: String, val usePoint: Int, val material: Material) {

  EXPLOSION(1, "ef_explosion", ChatColor.RED.toString() + "エクスプロージョン", "単純な爆発", 50, Material.TNT),
  BLIZZARD(2, "ef_blizzard", ChatColor.AQUA.toString() + "ブリザード", "凍らせる", 70, Material.PACKED_ICE),
  METEO(3, "ef_meteo", ChatColor.DARK_RED.toString() + "メテオ", "隕石を落とす", 100, Material.FIREBALL);

  internal var plugin = SeichiAssist.instance
  fun getsqlName(): String {
    return this.sql_name
  }

  fun getName(): String {
    return desc
  }

  //エフェクトの実行処理分岐 範囲破壊と複数範囲破壊
  fun runBreakEffect(player: Player, playerdata: PlayerData, tool: ItemStack, breaklist: List<Block>, start: Coordinate, end: Coordinate, standard: Location) {
    when (this) {
      EXPLOSION -> ExplosionTask(player, playerdata, tool, breaklist, start, end, standard).runTaskLater(plugin, 0)
      BLIZZARD -> if (playerdata.activeskilldata.skillnum < 3) {
        BlizzardTask(player, playerdata, tool, breaklist, start, end, standard).runTaskLater(plugin, 1)
      } else {
        if (SeichiAssist.DEBUG) {
          BlizzardTask(player, playerdata, tool, breaklist, start, end, standard).runTaskTimer(plugin, 0, 100)
        } else {
          BlizzardTask(player, playerdata, tool, breaklist, start, end, standard).runTaskTimer(plugin, 0, 10)
        }

      }
      METEO -> if (playerdata.activeskilldata.skillnum < 3) {
        MeteoTask(player, playerdata, tool, breaklist, start, end, standard).runTaskLater(plugin, 1)
      } else {
        MeteoTask(player, playerdata, tool, breaklist, start, end, standard).runTaskLater(plugin, 10)
      }
    }
  }

  //エフェクトの実行処理分岐
  fun runProjectileEffect(player: Player) {
    async {
      // https://discordapp.com/channels/237758724121427969/565935041574731807/589097781088616500
      repeat (100) {
        waitFor(1)
        when (this@ActiveSkillEffect) {
          EXPLOSION -> ArrowExplosionTask(player)
          BLIZZARD -> ArrowBlizzardTask(player)
          METEO -> ArrowMeteoTask(player)
        }
      }
    }
  }

  fun runAssaultEffect(player: Player, playerdata: PlayerData,
                       tool: ItemStack, arrayList: ArrayList<Block>, start: Coordinate,
                       end: Coordinate, centerofblock: Location) {
    when (this) {
      EXPLOSION -> player.world.spawnParticle(Particle.EXPLOSION_NORMAL, player.eyeLocation, 1, 3.0, 3.0, 3.0, 1.0)
      BLIZZARD -> player.world.spawnParticle(Particle.SNOW_SHOVEL, player.eyeLocation, 1, 3.0, 3.0, 3.0, 1.0)
      METEO -> player.world.spawnParticle(Particle.DRIP_LAVA, player.eyeLocation, 1, 3.0, 3.0, 3.0, 1.0)
    }
  }

  private fun async(action: suspend BukkitSchedulerController.() -> Unit) {
    Bukkit.getScheduler().schedule(SeichiAssist.instance, SynchronizationContext.ASYNC, action)
  }

  companion object {


    fun getNamebyNum(effectnum: Int): String {
      val skilleffect = values()
      return Arrays.stream(skilleffect)
          .filter { activeSkillEffect -> activeSkillEffect.num == effectnum }
          .findFirst()
          .map { it.getName() }
          .orElse("未設定")
    }

    fun fromSqlName(sqlName: String): ActiveSkillEffect? {
      return Arrays
          .stream(values())
          .filter { effect -> sqlName == effect.sql_name }
          .findFirst()
          .orElse(null)
    }
  }
}
