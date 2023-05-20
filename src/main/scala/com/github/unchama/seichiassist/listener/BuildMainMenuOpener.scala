package com.github.unchama.seichiassist.listener

import cats.effect.IO
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.seichiassist.effects.player.CommonSoundEffects
import com.github.unchama.seichiassist.menus.BuildMainMenu
import org.bukkit.Material
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.EquipmentSlot

class BuildMainMenuOpener(
  implicit effectEnvironment: EffectEnvironment,
  ioCanOpenBuildMainMenu: IO CanOpen BuildMainMenu.type
) extends Listener {

  import com.github.unchama.targetedeffect._

  @EventHandler
  def onPlayerLeftClickWithStick(event: PlayerInteractEvent): Unit = {
    val player = event.getPlayer

    event.getAction match {
      case Action.LEFT_CLICK_AIR | Action.LEFT_CLICK_BLOCK =>
      case _                                               => return
    }

    {
      val hasStickOnMainHand = player.getInventory.getItemInMainHand.getType == Material.STICK
      val actionWasOnMainHand = event.getHand == EquipmentSlot.HAND

      if (!hasStickOnMainHand || !actionWasOnMainHand) return
    }

    effectEnvironment.unsafeRunAsyncTargetedEffect(player)(
      SequentialEffect(
        CommonSoundEffects.menuTransitionFenceSound,
        ioCanOpenBuildMainMenu.open(BuildMainMenu)
      ),
      "BuildMainMenuを開く"
    )

    event.setCancelled(true)
  }
}
