package com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject

import com.github.unchama.minecraft.objects.{MinecraftItemStack, MinecraftMaterial}

case class MineStackObject[ItemStack](
  mineStackObjectName: String,
  uiName: Option[String],
  private val _itemStack: ItemStack,
  hasNameLore: Boolean, // 記名があるかどうか
  category: MineStackObjectCategory
) {

  def itemStack(implicit minecraftItemStack: MinecraftItemStack[ItemStack]): ItemStack =
    minecraftItemStack.clone(_itemStack)

  def durability(implicit minecraftItemStack: MinecraftItemStack[ItemStack]): Short =
    minecraftItemStack.durability(_itemStack)

}

object MineStackObject {

  def MineStackObjectByMaterial[ItemStack, Material](
    category: MineStackObjectCategory,
    mineStackObjectName: String,
    japaneseName: String,
    material: Material,
    durability: Short
  )(
    implicit minecraftMaterial: MinecraftMaterial[Material, ItemStack]
  ): MineStackObject[ItemStack] = {
    MineStackObject(
      mineStackObjectName,
      Some(japaneseName),
      minecraftMaterial.toItemStack(material, durability),
      hasNameLore = false,
      category
    )
  }

  def MineStackObjectByItemStack[ItemStack](
    category: MineStackObjectCategory,
    mineStackObjectName: String,
    japaneseName: Option[String],
    hasNameLore: Boolean,
    itemStack: ItemStack
  ): MineStackObject[ItemStack] = {
    MineStackObject(mineStackObjectName, japaneseName, itemStack, hasNameLore, category)
  }

}
