package com.github.unchama.seichiassist.task

import com.github.unchama.seichiassist.{ActiveSkillEffect, ActiveSkillPremiumEffect, SeichiAssist}
import com.github.unchama.seichiassist.data.XYZTuple
import com.github.unchama.seichiassist.util.BreakUtil
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

import scala.jdk.CollectionConverters
import scala.jdk.CollectionConverters._

class MultiBreakTask(var player: Player,
                     val centerblock: Block,
                     var tool: ItemStack,
                     var multibreaklist: List[List[Block]],
                     var multilavalist: List[List[Block]],
                     var startlist: List[XYZTuple],
                     var endlist: List[XYZTuple]) extends BukkitRunnable {

  def this(player: Player,
           centerblock: Block,
           tool: ItemStack,
           multibreaklist: java.util.List[java.util.List[Block]],
           multilavalist: java.util.List[java.util.List[Block]],
           startlist: java.util.List[XYZTuple],
           endlist: java.util.List[XYZTuple]) {
    this(
      player,
      centerblock,
      tool,
      multibreaklist.asScala.map(_.asScala.toList).toList,
      multilavalist.asScala.map(_.asScala.toList).toList,
      startlist.asScala.toList,
      endlist.asScala.toList)
  }

  private val playermap = SeichiAssist.playermap
  private val droploc = centerblock.getLocation.add(0.5, 0.5, 0.5)
  private val breaknum = multibreaklist.size
  private val playerdata = playermap.apply(player.getUniqueId)

  private var count = 0

  override def run(): Unit =
    if (count < breaknum) {
      if (SeichiAssist.DEBUG) player.sendMessage("" + count)

      //溶岩の破壊する処理
      (0 until multilavalist(count).size).foreach { lavanum =>
        multilavalist(count)(lavanum).setType(Material.AIR)
      }

      val breakBlocks = multibreaklist(count).toSet
      val startPoint = startlist(count)
      val endPoint = endlist(count)

      //エフェクトが選択されていない時の通常処理
      if (playerdata.activeskilldata.effectnum == 0) {
        com.github.unchama.seichiassist.unsafe.runIOAsync(
          "ブロックを大量破壊する",
          BreakUtil.massBreakBlock(player, breakBlocks, droploc, tool, shouldPlayBreakSound = false, Material.AIR)
        )
        SeichiAssist.managedBlocks --= breakBlocks
      } else {
        if (playerdata.activeskilldata.effectnum <= 100) {
          //通常エフェクトが指定されているときの処理(100以下の番号に割り振る）
          val skilleffect = ActiveSkillEffect.values
          skilleffect
            .apply(playerdata.activeskilldata.effectnum - 1)
            .runBreakEffect(player, playerdata.activeskilldata, tool, breakBlocks, startPoint, endPoint, droploc)
        } else {
          //スペシャルエフェクトが指定されているときの処理(１０１からの番号に割り振る）
          if (playerdata.activeskilldata.effectnum > 100) {
            val premiumeffect = ActiveSkillPremiumEffect.values
            premiumeffect
              .apply(playerdata.activeskilldata.effectnum - 1 - 100)
              .runBreakEffect(player, tool, breakBlocks, startPoint, endPoint, droploc)
          }
        }
      }
      count += 1
    } else
      cancel()
}