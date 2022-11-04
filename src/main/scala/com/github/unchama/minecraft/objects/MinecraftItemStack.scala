package com.github.unchama.minecraft.objects

/**
 * [[ItemStack]]に対する抽象
 */
trait MinecraftItemStack[ItemStack] {

  /**
   * @return [[ItemStack]]をクローンして返します。
   */
  def clone(): ItemStack

}
