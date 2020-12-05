package com.github.unchama.seasonalevents.christmas

import java.util.Random

import com.github.unchama.seasonalevents.christmas.ChristmasItemData._
import com.github.unchama.seichiassist.util.Util.{addItem, removeItemfromPlayerInventory}
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.event.block.Action
import org.bukkit.event.player.{PlayerInteractEvent, PlayerItemConsumeEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.{PotionEffect, PotionEffectType}

import scala.util.chaining._

object ChristmasItemListener extends Listener {
  @EventHandler
  def onPlayerConsumeChristmasCake(event: PlayerInteractEvent): Unit = {
    val item = event.getItem
    if (!isChristmasCake(item)) return

    if (event.getHand == EquipmentSlot.OFF_HAND) return
    if (event.getAction != Action.RIGHT_CLICK_AIR && event.getAction != Action.LEFT_CLICK_BLOCK) return

    event.setCancelled(true)

    val player = event.getPlayer

    val rand = new Random().nextDouble()
    val potionEffectType = if (rand > 0.5) PotionEffectType.LUCK else PotionEffectType.UNLUCK
    player.addPotionEffect(new PotionEffect(potionEffectType, 20 * 30, 0), true)

    removeItemfromPlayerInventory(player.getInventory, item, 1)

    val remainingPiece = new NBTItem(item).getByte(NBTTagConstants.cakePieceTag).toInt
    if (remainingPiece != 0) {
      val newItem = new NBTItem(item)
        .tap(_.setByte(NBTTagConstants.cakePieceTag, (remainingPiece - 1).toByte))
        .pipe(_.getItem)
      addItem(player, newItem)
    }
  }

  @EventHandler
  def onPlayerConsumeChristmasTurkey(event: PlayerItemConsumeEvent): Unit = {
    if (!isChristmasTurkey(event.getItem)) return

    val rand = new Random().nextDouble()
    val potionEffectType = if (rand > 0.5) PotionEffectType.SPEED else PotionEffectType.SLOW
    event.getPlayer.addPotionEffect(new PotionEffect(potionEffectType, 20 * 30, 0), true)
  }
}