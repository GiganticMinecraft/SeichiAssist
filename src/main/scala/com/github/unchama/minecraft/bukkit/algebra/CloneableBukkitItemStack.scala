package com.github.unchama.minecraft.bukkit.algebra

import com.github.unchama.generic.Cloneable
import org.bukkit.inventory.ItemStack

class CloneableBukkitItemStack extends Cloneable[ItemStack] {

  override def clone(itemStack: ItemStack): ItemStack = itemStack.clone()

}

object CloneableBukkitItemStack {

  implicit val instance: Cloneable[ItemStack] = new CloneableBukkitItemStack

}
