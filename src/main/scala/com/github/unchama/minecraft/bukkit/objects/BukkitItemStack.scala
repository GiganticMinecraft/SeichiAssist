package com.github.unchama.minecraft.bukkit.objects

import com.github.unchama.minecraft.objects.MinecraftItemStack
import org.bukkit.inventory.ItemStack

class BukkitItemStack extends MinecraftItemStack[ItemStack] {

  /**
   * @return [[ItemStack]]をコピーして返します。
   */
  override def copy(itemStack: ItemStack): ItemStack = itemStack.clone()
}
