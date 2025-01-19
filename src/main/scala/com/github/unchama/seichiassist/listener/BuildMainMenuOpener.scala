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
    val action = event.getAction

    if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) return

    val hasNotStickOnMainHand =
      player.getInventory.getItemInMainHand.getType != Material.STICK
    val actionWasNotOnMainHand = event.getHand != EquipmentSlot.HAND

    if (hasNotStickOnMainHand || actionWasNotOnMainHand) return

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
