package com.github.unchama.seichiassist.task

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.activeskill.effect.ActiveSkillEffect
import com.github.unchama.seichiassist.data.AxisAlignedCuboid
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

class MultiBreakTask(var player: Player,
                     val centerblock: Block,
                     var tool: ItemStack,
                     var multibreaklist: List[List[Block]],
                     var multilavalist: List[List[Block]],
                     val areaList: List[AxisAlignedCuboid]) extends BukkitRunnable {

  private val playermap = SeichiAssist.playermap
  private val droploc = centerblock.getLocation.add(0.5, 0.5, 0.5)
  private val breaknum = multibreaklist.size
  private val playerdata = playermap.apply(player.getUniqueId)

  private var count = 0

  override def run(): Unit =
    if (count < breaknum) {
      if (SeichiAssist.DEBUG) player.sendMessage("" + count)

      multilavalist(count).foreach(_.setType(Material.AIR))

      val breakBlocks = multibreaklist(count).toSet

      ActiveSkillEffect
        .fromEffectNum(playerdata.activeskilldata.effectnum)
        .runBreakEffect(player, playerdata.activeskilldata, tool, breakBlocks, areaList(count), droploc)

      count += 1
    } else
      cancel()
}
