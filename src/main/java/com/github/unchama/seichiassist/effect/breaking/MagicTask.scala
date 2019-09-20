package com.github.unchama.seichiassist.effect.breaking

import com.github.unchama.seichiassist.effect.XYZTuple
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MagicTask(// プレイヤー情報
    private val player: Player, // 使用するツール
    private val tool: ItemStack, // 破壊するブロックリスト
    private val blocks: Set[Block],
    start: XYZTuple,
    end: XYZTuple, skillCenter: Location) : RoundedTask() {
  // 破壊するブロックの中心位置
  private val centerBreak: Location
  // スキルが発動される中心位置
  private val skillCenter: Location = skillCenter.clone()

  init {
    centerBreak = this.skillCenter.add(relativeAverage(start.x, end.x), relativeAverage(start.y, end.y), relativeAverage(start.z, end.z))
  }

  override def firstAction() {
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

  override def secondAction() {
    //2回目のrun
    if (SeichiAssist.entitylist.isEmpty()) {
      val e = player.world.spawnEntity(centerBreak, EntityType.CHICKEN) as Chicken
      SeichiAssist.entitylist += e
      e.playEffect(EntityEffect.WITCH_MAGIC)
      e.isInvulnerable = true
      AsyncEntityRemover(e).runTaskLater(SeichiAssist.instance, 100)
      player.world.playSound(player.location, Sound.ENTITY_WITCH_AMBIENT, 1f, 1.5f)
    }

    for (b in blocks) {
      b.type = Material.AIR
      b.world.spawnParticle(Particle.NOTE, b.location.add(0.5, 0.5, 0.5), 1)
      SeichiAssist.allblocklist -= b
    }
    cancel()
  }

  private def relativeAverage(i1: Int, i2: Int): Double {
    return (i1 + (i2 - i1) / 2).toDouble()
  }
}
