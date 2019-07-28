package com.github.unchama.seichiassist

import com.github.unchama.seichiassist.data.Coordinate
import com.github.unchama.seichiassist.data.PlayerData
import com.github.unchama.seichiassist.effect.arrow.ArrowEffects
import com.github.unchama.seichiassist.effect.breaking.BlizzardTask
import com.github.unchama.seichiassist.effect.breaking.ExplosionTask
import com.github.unchama.seichiassist.effect.breaking.MeteoTask
import com.github.unchama.seichiassist.effect.toXYZTuple
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

enum class ActiveSkillEffect constructor(
    val num: Int,
    val nameOnDatabase: String,
    val nameOnUI: String,
    val explanation: String,
    val usePoint: Int,
    val material: Material) {

  EXPLOSION(1, "ef_explosion", "${ChatColor.RED}エクスプロージョン", "単純な爆発", 50, Material.TNT),
  BLIZZARD(2, "ef_blizzard", "${ChatColor.AQUA}ブリザード", "凍らせる", 70, Material.PACKED_ICE),
  METEO(3, "ef_meteo", "${ChatColor.DARK_RED}メテオ", "隕石を落とす", 100, Material.FIREBALL);

  internal var plugin = SeichiAssist.instance

  //エフェクトの実行処理分岐 範囲破壊と複数範囲破壊
  fun runBreakEffect(player: Player,
                     playerdata: PlayerData,
                     tool: ItemStack,
                     breaklist: Set<Block>,
                     start: Coordinate, end: Coordinate,
                     standard: Location) {
    when (this) {
      EXPLOSION -> ExplosionTask(player, playerdata, tool, breaklist, start.toXYZTuple(), end.toXYZTuple(), standard).runTask(plugin)
      BLIZZARD -> {
        val effect = BlizzardTask(player, playerdata, tool, breaklist, start, end, standard)

        if (playerdata.activeskilldata.skillnum < 3) {
          effect.runTaskLater(plugin, 1)
        } else {
          val period = if (SeichiAssist.DEBUG) 100L else 10L
          effect.runTaskTimer(plugin, 0, period)
        }
      }
      METEO -> {
        val delay = if (playerdata.activeskilldata.skillnum < 3) 1L else 10L

        MeteoTask(player, playerdata, tool, breaklist, start, end, standard)
            .runTaskLater(plugin, delay)
      }
    }
  }

  //エフェクトの実行処理分岐
  fun runArrowEffect(player: Player) {
    val effect = when (this@ActiveSkillEffect) {
      EXPLOSION -> ArrowEffects.singleArrowExplosionEffect
      BLIZZARD -> ArrowEffects.singleArrowBlizzardEffect
      METEO -> ArrowEffects.singleArrowMeteoEffect
    }

    GlobalScope.launch(Schedulers.async) {
      repeat (100) {
        effect.runFor(player)
        delay(50)
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

  companion object {
    fun getNamebyNum(effectnum: Int): String = values()
        .find { activeSkillEffect -> activeSkillEffect.num == effectnum }
        ?.let { it.nameOnUI } ?: "未設定"

    fun fromSqlName(sqlName: String): ActiveSkillEffect? = values()
        .find { effect -> sqlName == effect.nameOnDatabase }
  }
}
