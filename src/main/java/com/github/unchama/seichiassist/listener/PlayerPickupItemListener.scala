package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.BreakUtil
import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver
import org.bukkit.ChatColor._
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.{GameMode, Sound}

class PlayerPickupItemListener  extends  Listener {
  private val playerMap = SeichiAssist.playermap
  private val config = SeichiAssist.seichiAssistConfig

  @EventHandler
  def onPickupMineStackItem(event: PlayerPickupItemEvent) {
    val player = event.player

    if (player.gameMode !== GameMode.SURVIVAL) return

    val playerData = playerMap(player.getUniqueId).ifNull(return)

    if (playerData.level < config.getMineStacklevel(1)) return

    if (!playerData.settings.autoMineStack) return

    val item = event.item
    val itemstack = item.itemStack

    if (SeichiAssist.DEBUG) {
      player.sendMessage(RED.toString() + "pick:" + itemstack.toString())
      player.sendMessage(RED.toString() + "pickDurability:" + itemstack.durability)
    }

    if (BreakUtil.addItemToMineStack(player, itemstack)) {
      event.setCancelled(true)
      player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
      item.remove()
    }
  }
}
