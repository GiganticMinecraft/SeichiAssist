package com.github.unchama.buildassist.listener

import cats.effect.SyncIO
import com.github.unchama.buildassist.menu.BuildMainMenu
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.effects.player.CommonSoundEffects
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems
import org.bukkit.Material
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.EquipmentSlot

class PlayerLeftClickListener(implicit effectEnvironment: EffectEnvironment,
                              flySystem: StatefulSubsystem[subsystems.managedfly.InternalState[SyncIO]]) extends Listener {

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

    import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, syncShift}

    effectEnvironment.runAsyncTargetedEffect(player)(
      SequentialEffect(
        CommonSoundEffects.menuTransitionFenceSound,
        new BuildMainMenu().open
      ),
      "BuildMainMenuを開く"
    )

    event.setCancelled(true)
  }
}
