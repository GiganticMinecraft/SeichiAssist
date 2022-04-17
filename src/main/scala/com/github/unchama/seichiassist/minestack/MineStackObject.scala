package com.github.unchama.seichiassist.minestack

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

case class MineStackObject(
  mineStackObjectName: String,
  uiName: Option[String],
  itemStack: ItemStack,
  hasNameLore: Boolean, // 多分記名があるかどうか
  category: MineStackObjectCategory
) {

  def material: Material = itemStack.getType

  def durability: Short = itemStack.getDurability

}

object MineStackObject {

  def materialMineStackObject(
    category: MineStackObjectCategory,
    mineStackObjectName: String,
    japaneseName: String,
    material: Material,
    durability: Short
  ): MineStackObject = {
    MineStackObject(
      mineStackObjectName,
      Some(japaneseName),
      new ItemStack(material, 1, durability),
      hasNameLore = false,
      category
    )
  }

  def itemStackMineStackObject(
    category: MineStackObjectCategory,
    mineStackObjectName: String,
    japaneseName: Option[String],
    hasNameLore: Boolean,
    itemStack: ItemStack
  ): MineStackObject = {
    MineStackObject(mineStackObjectName, japaneseName, itemStack, hasNameLore, category)
  }

}
