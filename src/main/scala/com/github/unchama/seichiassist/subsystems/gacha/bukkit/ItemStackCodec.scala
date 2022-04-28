package com.github.unchama.seichiassist.subsystems.gacha.bukkit

import com.github.unchama.seichiassist.util.BukkitSerialization
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack

/**
 * [[org.bukkit.inventory.ItemStack]] と [[String]] との相互変換を実現するコーデック
 */
object ItemStackCodec {

  def fromBukkitItemStack(itemStack: ItemStack): String = {
    val inventory = Bukkit.getServer.createInventory(null, 9)
    inventory.setItem(0, itemStack)
    BukkitSerialization.toBase64(inventory)
  }

  def fromString(itemStack: String): ItemStack = {
    BukkitSerialization.fromBase64(itemStack).getItem(0)
  }

}
