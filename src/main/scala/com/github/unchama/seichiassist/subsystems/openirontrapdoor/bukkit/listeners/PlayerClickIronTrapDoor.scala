package com.github.unchama.seichiassist.subsystems.openirontrapdoor.bukkit.listeners

import com.github.unchama.util.external.WorldGuardWrapper.isRegionMember
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.material.Openable

object PlayerClickIronTrapDoor extends Listener {
  @EventHandler
  def onPlayerRightClickIronTrapDoor(event: PlayerInteractEvent): Unit = {
    val clickedBlock = event.getClickedBlock
    if (clickedBlock == null) return
    if (!isRegionMember(event.getPlayer, clickedBlock.getLocation)) return

    if (event.getHand != EquipmentSlot.HAND) return
    if (
      event.getAction != Action.RIGHT_CLICK_BLOCK || clickedBlock.getType != Material.IRON_TRAPDOOR
    ) return

    // TODO: 手に何も持っていない場合は機能するが、ブロックなどを持っている場合は機能しない（手に持っているものが設置できるもののときや弓矢は反応する）
    val blockData = clickedBlock.getBlockData
    blockData match {
      case openable: Openable =>
        openable.setOpen(!openable.isOpen)

        val blockState = clickedBlock.getState
        blockState.setBlockData(openable.asInstanceOf[BlockData])
        blockState.update()
      case _ =>
    }
  }
}
