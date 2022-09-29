package com.github.unchama.minecraft.objects

/**
 * Materialに対する抽象
 */
trait MinecraftMaterial[Material, ItemStack] {

  def toItemStack(material: Material, durability: Short): ItemStack

}
