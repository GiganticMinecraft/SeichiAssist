package com.github.unchama.seichiassist.effect.breaking

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.Coordinate
import com.github.unchama.seichiassist.data.PlayerData
import com.github.unchama.seichiassist.effect.XYZIterator2
import com.github.unchama.seichiassist.effect.XYZTuple
import com.github.unchama.seichiassist.util.BreakUtil
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ExplosionTask(private val player: Player, private val playerdata: PlayerData, private val tool: ItemStack, //破壊するブロックリスト
                    override val blocks: List<Block>, //スキルで破壊される相対座標
                    private val start: Coordinate,
                    private val end: Coordinate, //スキルが発動される中心位置
                    private val droploc: Location) : AbstractBreakTask2() {

  override fun run() {
    XYZIterator2(XYZTuple(start.x, start.y, start.z), XYZTuple(end.x, end.y, end.z)) { xyzTuple ->
      val explosionloc = droploc.clone()
      explosionloc.add(xyzTuple.x.toDouble(), xyzTuple.y.toDouble(), xyzTuple.z.toDouble())
      if (isBreakBlock(explosionloc)) {
        player.world.createExplosion(explosionloc, 0f, false)
      }
      Unit
    }

    val stepflag = playerdata.activeskilldata.skillnum <= 2
    for (b in blocks) {
      BreakUtil.breakBlock(player, b, droploc, tool, stepflag)
      SeichiAssist.allblocklist.remove(b)
    }
  }
}

