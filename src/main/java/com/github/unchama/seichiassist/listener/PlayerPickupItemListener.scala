package com.github.unchama.seichiassist.listener

class PlayerPickupItemListener : Listener {
  private val playerMap = SeichiAssist.playermap
  private val config = SeichiAssist.seichiAssistConfig

  @EventHandler
  def onPickupMineStackItem(event: PlayerPickupItemEvent) {
    val player = event.player

    if (player.gameMode !== GameMode.SURVIVAL) return

    val playerData = playerMap[player.uniqueId] ?: return

    if (playerData.level < config.getMineStacklevel(1)) return

    if (!playerData.settings.autoMineStack) return

    val item = event.item
    val itemstack = item.itemStack

    if (SeichiAssist.DEBUG) {
      player.sendMessage(ChatColor.RED.toString() + "pick:" + itemstack.toString())
      player.sendMessage(ChatColor.RED.toString() + "pickDurability:" + itemstack.durability)
    }

    if (BreakUtil.addItemToMineStack(player, itemstack)) {
      event.isCancelled = true
      player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
      item.remove()
    }
  }
}
