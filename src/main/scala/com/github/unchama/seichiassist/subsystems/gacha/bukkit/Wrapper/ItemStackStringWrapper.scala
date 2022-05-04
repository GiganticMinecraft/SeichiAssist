package com.github.unchama.seichiassist.subsystems.gacha.bukkit.Wrapper

import com.github.unchama.seichiassist.util.BukkitSerialization
import org.bukkit.inventory.ItemStack

case class ItemStackStringWrapper(itemStack: String, amount: Int) {

  def toItemStack: ItemStack =
    BukkitSerialization.fromBase64(itemStack).getItem(0)

}
