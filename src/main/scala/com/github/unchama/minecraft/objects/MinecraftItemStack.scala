package com.github.unchama.minecraft.objects

/**
 * ItemStackに対する操作の抽象
 */
trait MinecraftItemStack[ItemStack] {

  def clone(itemStack: ItemStack): ItemStack

  def durability(itemStack: ItemStack): Short

}
