package com.github.unchama.seichiassist.minestack

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class MineStackObj(val mineStackObjName: String,
                   val uiName: Option[String],
                   val level: Int,
                   private val _itemStack: ItemStack,
                   val hasNameLore: Boolean,
                   val gachaType: Int,
                   val stackType: MineStackObjectCategory) {

  val itemStack: ItemStack = {
    val cloned = _itemStack.clone()
    cloned.setAmount(1)
    cloned
  }

  def this(objName: String, uiName: Option[String],
           level: Int, material: Material, durability: Int,
           nameLoreFlag: Boolean, gachaType: Int, stackType: MineStackObjectCategory) =
    this(
      objName, uiName, level, new ItemStack(material, 1, durability.toShort), nameLoreFlag, gachaType, stackType
    )

  def material: Material = itemStack.getType

  def durability: Int = itemStack.getDurability.toInt

  override def equals(other: Any): Boolean = other match {
    case that: MineStackObj =>
      (that canEqual this) &&
        mineStackObjName == that.mineStackObjName
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[MineStackObj]

  override def hashCode(): Int = {
    val state = Seq(mineStackObjName)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
