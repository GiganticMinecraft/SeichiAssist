package com.github.unchama.minecraft.objects

/**
 * [[ItemStack]]に対する抽象
 */
trait MinecraftItemStack[ItemStack] {

  /**
   * @return [[ItemStack]]をコピーして返します。
   */
  def copy(itemStack: ItemStack): ItemStack

}
