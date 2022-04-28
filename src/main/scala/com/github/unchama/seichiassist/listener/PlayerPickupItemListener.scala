package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.BreakUtil
import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.{GameMode, Sound}

class PlayerPickupItemListener extends Listener {
  private val playerMap = SeichiAssist.playermap

  @EventHandler
  def onPickupMineStackItem(event: EntityPickupItemEvent): Unit = {
    event.getEntity match {
      case _: Player =>
      case player: Player =>
        if (player.getGameMode != GameMode.SURVIVAL) return

        val playerData = playerMap(player.getUniqueId).ifNull(
          return
        )

        if (!playerData.settings.autoMineStack) return

        val item = event.getItem
        val itemstack = item.getItemStack

        if (SeichiAssist.DEBUG) {
          player.sendMessage(RED.toString + "pick:" + itemstack.toString)
          player.sendMessage(RED.toString + "pickDurability:" + itemstack.getDurability)
        }

        if (BreakUtil.tryAddItemIntoMineStack(player, itemstack)) {
          event.setCancelled(true)
          player.playSound(player.getLocation, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
          item.remove()
        }
    }
  }
}
