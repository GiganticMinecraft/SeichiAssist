package com.github.unchama.seichiassist.effect.breaking

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.Coordinate
import com.github.unchama.seichiassist.data.PlayerData
import com.github.unchama.seichiassist.effect.XYZIterator2
import com.github.unchama.seichiassist.effect.XYZTuple
import com.github.unchama.seichiassist.util.BreakUtil
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class MeteoTask(//プレイヤー情報
    private val player: Player, //プレイヤーデータ
    private val playerdata: PlayerData, //使用するツール
    private val tool: ItemStack, //破壊するブロックリスト
    override val blocks: List<Block>, //スキルで破壊される相対座標
    private val start: Coordinate,
    private val end: Coordinate, droploc: Location) : AbstractBreakTask2() {
  //破壊するブロックの中心位置
  private val centerbreakloc: Location
  //スキルが発動される中心位置
  private val droploc: Location

  init {
    this.droploc = droploc.clone()
    this.centerbreakloc = this.droploc.add(relativeAverage(start.x, end.x), relativeAverage(start.y, end.y), relativeAverage(start.z, end.z))
  }

  override fun run() {
    XYZIterator2(XYZTuple(start.x, start.y, start.z), XYZTuple(end.x, end.y, end.z)) { xyzTuple ->
      //逐一更新が必要な位置
      val effectloc = droploc.clone().add(xyzTuple.x.toDouble(), xyzTuple.y.toDouble(), xyzTuple.z.toDouble())
      if (isBreakBlock(effectloc)) {
        // TODO: Effect.EXPLOSION_HUGE -> Particle.EXPLOSION_HUGE
        player.world.playEffect(effectloc, Effect.EXPLOSION_HUGE, 1)
      }
      Unit
    }.doAction()
    // 0..1 -> 0..0.4 -> 0.8..1.2
    val vol = Random().nextFloat() * 0.4f + 0.8f
    player.world.playSound(centerbreakloc, Sound.ENTITY_WITHER_BREAK_BLOCK, 1.0f, vol)
    val stepflag = playerdata.activeskilldata.skillnum <= 2
    for (b in blocks) {
      BreakUtil.breakBlock(player, b, droploc, tool, stepflag)
      SeichiAssist.allblocklist.remove(b)
    }
  }

  private fun relativeAverage(i1: Int, i2: Int): Double {
    return (i1 + (i2 - i1) / 2).toDouble()
  }
}

