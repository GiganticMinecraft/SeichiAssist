package com.github.unchama.seichiassist.subsystems.gacha.bukkit.codec

import com.github.unchama.seichiassist.util.BukkitSerialization
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack

object ItemStackCodec {

  def fromString(itemStack: String): ItemStack =
    BukkitSerialization.fromBase64(itemStack).getItem(0)

  def toString(itemStack: ItemStack): String = {
    val inventory = Bukkit.getServer.createInventory(null, 9)
    inventory.setItem(0, itemStack)
    BukkitSerialization.toBase64(inventory)
  }

}
