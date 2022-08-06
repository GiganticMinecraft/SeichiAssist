package com.github.unchama.minecraft.bukkit.algebra

import com.github.unchama.generic.serialization.SerializeAndDeserialize
import com.github.unchama.seichiassist.util.BukkitSerialization
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack

class BukkitItemStackSerializeAndDeserialize extends SerializeAndDeserialize[Unit, ItemStack] {

  override def serialize(itemStack: ItemStack): String = {
    val inventory = Bukkit.getServer.createInventory(null, 9)
    inventory.setItem(0, itemStack)
    BukkitSerialization.toBase64(inventory)
  }

  override def deserialize(itemStackStr: String): Either[Unit, ItemStack] =
    Right(BukkitSerialization.fromBase64(itemStackStr).getItem(0))

}

object BukkitItemStackSerializeAndDeserialize {

  val instance: SerializeAndDeserialize[Unit, ItemStack] =
    new BukkitItemStackSerializeAndDeserialize

}
