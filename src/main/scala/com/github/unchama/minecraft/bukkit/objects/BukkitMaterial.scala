package com.github.unchama.minecraft.bukkit.objects

import com.github.unchama.minecraft.objects.MinecraftMaterial
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class BukkitMaterial extends MinecraftMaterial[Material, ItemStack] {
  override def toItemStack(material: Material, durability: Short): ItemStack =
    new ItemStack(material, 1, durability)
}
