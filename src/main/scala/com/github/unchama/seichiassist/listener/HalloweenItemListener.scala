package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.data.HalloweenItemData.{isHalloweenHoe, isHalloweenPotion}
import com.github.unchama.util.external.WorldGuardWrapper.isRegionMember
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.{PlayerInteractEvent, PlayerItemConsumeEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.{PotionEffect, PotionEffectType}
import org.bukkit.{Location, Material}

class HalloweenItemListener extends Listener {

  @EventHandler
  def onPlayerConsumeHalloweenPotion(event: PlayerItemConsumeEvent): Unit = {
    if (isHalloweenPotion(event.getItem)) {
      // 1.12.2では、Saturationのポーションは効果がないので、PotionEffectとして直接Playerに付与する
      // 10分
      event.getPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 60 * 10, 0), true)
    }
  }

  @EventHandler
  def onPlayerRightClickWithHalloweenHoe(event: PlayerInteractEvent): Unit = {
    if (!isHalloweenHoe(event.getItem)) return

    // 特殊エンチャント：
    if (event.getHand == EquipmentSlot.OFF_HAND) return
    if (event.getAction != Action.RIGHT_CLICK_BLOCK) return

    val clickedBlock = event.getClickedBlock
    if (clickedBlock == null) return
    val clickedBlockLoc = clickedBlock.getLocation
    val standardLoc = (clickedBlockLoc.getX.toInt, clickedBlockLoc.getZ.toInt)

    val player = event.getPlayer
    // まず、Playerが自分でクリックしたブロックについて判定する
    if (!canBeReplacedWithSoil(player, clickedBlockLoc)) return
    clickedBlock.setType(Material.SOIL)

    // 次にクリックされたブロックから半径4ブロック以内のブロックについて判定する
    for (x <- standardLoc._1 - 4 to standardLoc._1 + 4) {
      for (z <- standardLoc._2 - 4 to standardLoc._2 + 4) {
        val loc = new Location(clickedBlockLoc.getWorld, x.toDouble, clickedBlockLoc.getY, z.toDouble)
        val block = loc.getBlock

        if (block != null && canBeReplacedWithSoil(player, loc)) block.setType(Material.SOIL)
      }
    }
  }

  private def canBeReplacedWithSoil(player: Player, loc: Location) = {
    val block = loc.getBlock
    isRegionMember(player, loc) && (block.getType == Material.DIRT || block.getType == Material.GRASS)
  }
}