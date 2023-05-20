package com.github.unchama.minecraft.bukkit.algebra

import com.github.unchama.generic.serialization.SerializeAndDeserialize
import com.github.unchama.seichiassist.util.BukkitSerialization
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack

object BukkitItemStackSerializeAndDeserialize
    extends SerializeAndDeserialize[Nothing, ItemStack] {

  override def serialize(itemStack: ItemStack): String = {
    val inventory = Bukkit.getServer.createInventory(null, 9)
    inventory.setItem(0, itemStack)
    BukkitSerialization.toBase64(inventory)
  }

  override def deserialize(serialized: String): Either[Nothing, ItemStack] =
    Right(BukkitSerialization.fromBase64(serialized).getItem(0))

}
