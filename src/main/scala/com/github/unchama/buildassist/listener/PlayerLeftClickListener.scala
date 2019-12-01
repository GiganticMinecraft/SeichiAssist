package com.github.unchama.buildassist.listener

import com.github.unchama.buildassist.menu.BuildMainMenu
import com.github.unchama.seichiassist
import com.github.unchama.seichiassist.CommonSoundEffects
import org.bukkit.Material
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.EquipmentSlot

object PlayerLeftClickListener extends Listener {

  import com.github.unchama.targetedeffect._

  @EventHandler
  def onPlayerLeftClickWithStick(event: PlayerInteractEvent): Unit = {
    val player = event.getPlayer

    event.getAction match {
      case Action.LEFT_CLICK_AIR | Action.LEFT_CLICK_BLOCK =>
      case _ => return
    }

    {
      val hasStickOnMainHand = player.getInventory.getItemInMainHand.getType == Material.STICK
      val actionWasOnMainHand = event.getHand == EquipmentSlot.HAND

      if (!hasStickOnMainHand || !actionWasOnMainHand) return
    }

    import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, sync}

    seichiassist.unsafe.runAsyncTargetedEffect(player)(
      sequentialEffect(
        CommonSoundEffects.menuTransitionFenceSound,
        BuildMainMenu.open
      ),
      "BuildMainMenuを開く"
    )

    event.setCancelled(true)
  }
}
