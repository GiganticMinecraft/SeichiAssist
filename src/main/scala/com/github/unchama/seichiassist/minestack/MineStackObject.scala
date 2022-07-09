package com.github.unchama.seichiassist.minestack

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class MineStackObject(
  val mineStackObjectName: String,
  val uiName: Option[String],
  private val _itemStack: ItemStack,
  val hasNameLore: Boolean, // 多分記名があるかどうか
  val category: MineStackObjectCategory
) {

  def itemStack: ItemStack = _itemStack.clone()

  def material: Material = _itemStack.getType

  def durability: Short = _itemStack.getDurability

}

object MineStackObject {

  def materialMineStackObject(
    category: MineStackObjectCategory,
    mineStackObjectName: String,
    japaneseName: String,
    material: Material,
    durability: Short
  ): MineStackObject = {
    new MineStackObject(
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
    new MineStackObject(mineStackObjectName, japaneseName, itemStack, hasNameLore, category)
  }

}
