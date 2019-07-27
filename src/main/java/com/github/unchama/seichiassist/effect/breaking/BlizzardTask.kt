package com.github.unchama.seichiassist.effect.breaking

import com.github.unchama.seichiassist.ActiveSkill
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.Coordinate
import com.github.unchama.seichiassist.data.PlayerData
import com.github.unchama.seichiassist.effect.AxisAlignedCuboid
import com.github.unchama.seichiassist.effect.XYZTuple
import com.github.unchama.seichiassist.effect.forEachGridPoint
import com.github.unchama.seichiassist.util.BreakUtil
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BlizzardTask(private val player: Player, private val playerdata: PlayerData,
                   private val tool: ItemStack,
                   private val blocks: List<Block>,
                   private val start: Coordinate,
                   private val end: Coordinate,
                   private val droploc: Location) : AbstractRoundedTask() {
  //音の聞こえる距離
  private var soundRadius: Int = 0
  private var setRadius: Boolean = false

  override fun firstAction() {
    //1回目のrun
    if (playerdata.activeskilldata.skillnum > 2) {
      for (b in blocks) {
        BreakUtil.breakBlock(player, b, droploc, tool, false)
        b.type = Material.PACKED_ICE
      }
    } else {
      for (b in blocks) {
        BreakUtil.breakBlock(player, b, droploc, tool, true)
        SeichiAssist.allblocklist.remove(b)
      }
      cancel()
    }
    soundRadius = 5
    setRadius = playerdata.activeskilldata.skilltype == ActiveSkill.BREAK.gettypenum()
  }

  override fun secondAction() {
    //2回目のrun
    AxisAlignedCuboid(XYZTuple(start.x, start.y, start.z), XYZTuple(end.x, end.y, end.z)).forEachGridPoint { xyzTuple ->
      //逐一更新が必要な位置
      val effectloc = droploc.clone().add(xyzTuple.x.toDouble(), xyzTuple.y.toDouble(), xyzTuple.z.toDouble())
      if (blocks.contains(effectloc.block)) {
        player.world.playEffect(effectloc, Effect.SNOWBALL_BREAK, 1)
      }
    }

    if (playerdata.activeskilldata.skillnum > 2) {
      for (b in blocks) {
        b.type = Material.AIR
        if (setRadius) {
          b.world.playEffect(b.location, Effect.STEP_SOUND, Material.PACKED_ICE, soundRadius)
        } else {
          b.world.playEffect(b.location, Effect.STEP_SOUND, Material.PACKED_ICE)
        }
        SeichiAssist.allblocklist.remove(b)
      }
    }
  }
}

