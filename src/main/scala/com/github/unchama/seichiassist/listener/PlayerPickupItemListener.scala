package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.BreakUtil
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.{GameMode, Sound}

class PlayerPickupItemListener extends Listener {
  private val playerMap = SeichiAssist.playermap
  private val config = SeichiAssist.seichiAssistConfig

  @EventHandler
  def onPickupMineStackItem(event: EntityPickupItemEvent): Unit = {
    val player = event.getEntity match {
      case player: Player => player
      case _ => return
    }

    if (player.getGameMode != GameMode.SURVIVAL) return

    val playerLevel = SeichiAssist
      .instance
      .breakCountSystem
      .api
      .seichiAmountDataRepository(player)
      .read
      .unsafeRunSync()
      .levelCorrespondingToExp
      .level

    if (playerLevel < config.getMineStacklevel(1)) return

    val playerData = playerMap.getOrElse(player.getUniqueId, return)
    if (!playerData.settings.autoMineStack) return

    val item = event.getItem
    val itemstack = item.getItemStack

    if (SeichiAssist.DEBUG) {
      player.sendMessage(s"${RED}pick:$itemstack")
      player.sendMessage(s"${RED}pickDurability:${itemstack.getDurability}")
    }

    if (BreakUtil.tryAddItemIntoMineStack(player, itemstack)) {
      event.setCancelled(true)
      player.playSound(player.getLocation, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
      item.remove()
    }
  }
}
