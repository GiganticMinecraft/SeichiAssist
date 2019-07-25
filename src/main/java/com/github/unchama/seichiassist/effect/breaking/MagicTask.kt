package com.github.unchama.seichiassist.effect.breaking

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.Coordinate
import com.github.unchama.seichiassist.data.PlayerData
import com.github.unchama.seichiassist.task.AsyncEntityRemover
import com.github.unchama.seichiassist.util.BreakUtil
import org.bukkit.DyeColor
import org.bukkit.EntityEffect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.entity.Chicken
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.material.Wool
import java.util.Random

class MagicTask(// プレイヤー情報
    private val player: Player, // 使用するツール
    private val tool: ItemStack, // 破壊するブロックリスト
    private val blocks: List<Block>, start: Coordinate,
    end: Coordinate, skillCenter: Location) : AbstractRoundedTask() {
  // 破壊するブロックの中心位置
  private val centerBreak: Location
  // スキルが発動される中心位置
  private val skillCenter: Location = skillCenter.clone()

  init {
    // スキルで破壊される相対座標

    centerBreak = this.skillCenter.add(relativeAverage(start.x, end.x), relativeAverage(start.y, end.y), relativeAverage(start.z, end.z))
  }

  override fun firstAction() {
    //1回目のrun
    val colors = arrayOf(DyeColor.RED, DyeColor.BLUE, DyeColor.YELLOW, DyeColor.GREEN)
    val rd = Random().nextInt(colors.size)

    for (b in blocks) {
      BreakUtil.breakBlock(player, b, skillCenter, tool, false)
      b.type = Material.WOOL
      val state = b.state
      val woolBlock = state.data as Wool
      woolBlock.color = colors[rd]
      state.update()
    }
  }

  override fun secondAction() {
    //2回目のrun
    if (SeichiAssist.entitylist.isEmpty()) {
      val e = player.world.spawnEntity(centerBreak, EntityType.CHICKEN) as Chicken
      SeichiAssist.entitylist.add(e)
      e.playEffect(EntityEffect.WITCH_MAGIC)
      e.isInvulnerable = true
      AsyncEntityRemover(e).runTaskLater(SeichiAssist.instance, 100)
      player.world.playSound(player.location, Sound.ENTITY_WITCH_AMBIENT, 1f, 1.5f)
    }

    for (b in blocks) {
      b.type = Material.AIR
      b.world.spawnParticle(Particle.NOTE, b.location.add(0.5, 0.5, 0.5), 1)
      SeichiAssist.allblocklist.remove(b)
    }
    cancel()
  }

  private fun relativeAverage(i1: Int, i2: Int): Double {
    return (i1 + (i2 - i1) / 2).toDouble()
  }
}
