package com.github.unchama.minecraft.bukkit.objects

import com.github.unchama.minecraft.objects.MinecraftItemStack
import org.bukkit.inventory.ItemStack

class BukkitItemStack extends MinecraftItemStack[ItemStack] {

  override def clone(itemStack: ItemStack): ItemStack = itemStack.clone()

  override def durability(itemStack: ItemStack): Short =
    itemStack.getDurability
}
