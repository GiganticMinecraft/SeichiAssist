package com.github.unchama.minecraft.objects

/**
 * [[Material]]に対する抽象
 */
trait MinecraftMaterial[Material, ItemStack] {

  def toItemStack(material: Material, durability: Short): ItemStack

}
