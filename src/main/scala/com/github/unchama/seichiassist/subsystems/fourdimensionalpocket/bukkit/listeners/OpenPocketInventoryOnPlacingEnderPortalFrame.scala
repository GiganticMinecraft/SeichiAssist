package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.bukkit.listeners

import cats.effect.Effect
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.FourDimensionalPocketApi
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.EquipmentSlot

class OpenPocketInventoryOnPlacingEnderPortalFrame[F[_]: Effect](
  api: FourDimensionalPocketApi[F, Player],
  effectEnvironment: EffectEnvironment
) extends Listener {

  @EventHandler
  def onInteractEvent(event: PlayerInteractEvent): Unit = {

    val player = event.getPlayer
    val action = event.getAction
    val hand = event.getHand

    if (event.getMaterial != Material.ENDER_PORTAL_FRAME) {
      return
    }

    if (
      hand == EquipmentSlot.OFF_HAND || !(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
    ) {
      return
    }

    // 設置をキャンセル
    event.setCancelled(true)

    effectEnvironment.unsafeRunEffectAsync("ポケットインベントリを開く", api.openPocketInventory(player))
  }
}
