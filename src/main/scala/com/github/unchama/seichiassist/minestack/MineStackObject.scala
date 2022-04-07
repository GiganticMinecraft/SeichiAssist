package com.github.unchama.seichiassist.minestack

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

case class MineStackObject private (
  mineStackObjectName: String,
  uiName: Option[String],
  itemStack: ItemStack,
  hasNameLore: Boolean,
  category: MineStackObjectCategory
)

object MineStackObject {

  def normalMineStackObject(
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
    itemStack: ItemStack
  ): MineStackObject = {
    MineStackObject(mineStackObjectName, japaneseName, itemStack, itemStack.getItemMeta.hasLore)
  }

}
