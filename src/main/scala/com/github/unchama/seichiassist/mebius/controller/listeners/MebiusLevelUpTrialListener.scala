package com.github.unchama.seichiassist.mebius.controller.listeners

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.mebius.controller.codec.ItemStackMebiusCodec
import com.github.unchama.seichiassist.mebius.domain.PropertyModificationMessages
import com.github.unchama.seichiassist.mebius.domain.resources.MebiusMessages
import com.github.unchama.seichiassist.mebius.service.MebiusLevellingService
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

class MebiusLevelUpTrialListener(implicit messages: PropertyModificationMessages) extends Listener {
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def tryMebiusLevelUpOn(event: BlockBreakEvent): Unit = {
    val player = event.getPlayer

    val oldMebiusProperty = ItemStackMebiusCodec.decodeMebiusProperty(player.getInventory.getHelmet).getOrElse(return)
    val newMebiusProperty = MebiusLevellingService.attemptLevelUp(oldMebiusProperty).unsafeRunSync()

    if (newMebiusProperty != oldMebiusProperty) {
      player.sendMessage {
        messages.onLevelUp(oldMebiusProperty, newMebiusProperty).toArray
      }

      SeichiAssist.playermap(player.getUniqueId).mebius
        .speakForce(MebiusMessages.talkOnLevelUp(newMebiusProperty.level).mebiusMessage)

      player.getInventory.setHelmet {
        ItemStackMebiusCodec.materialize(newMebiusProperty, damageValue = 0)
      }
    }
  }
}
