package com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject

import com.github.unchama.minecraft.objects.MinecraftMaterial
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrize

case class MineStackObject[ItemStack <: Cloneable](
  mineStackObjectName: String,
  uiName: Option[String],
  private val _itemStack: ItemStack,
  hasNameLore: Boolean,
  category: MineStackObjectCategory
) {

  def itemStack: ItemStack = _itemStack.clone[ItemStack]

  /**
   * [[GachaPrize]]への変換した後、記名済みの[[ItemStack]]へ変換することを試みます
   */
  def tryToSignedItemStack[F[_], Player](
    name: String
  )(implicit gachaAPI: GachaAPI[F, ItemStack, Player]): Option[ItemStack] = {
    if (
      category != MineStackObjectCategory.GACHA_PRIZES || category == MineStackObjectCategory.BUILTIN_GACHA_PRIZES
    ) return None


    // TODO ガチャAPIからcanBeSigned..を取得して記名して返す
    None
  }

}

object MineStackObject {

  def MineStackObjectByMaterial[ItemStack <: Cloneable, Material](
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

  def MineStackObjectByItemStack[ItemStack <: Cloneable](
    category: MineStackObjectCategory,
    mineStackObjectName: String,
    japaneseName: Option[String],
    hasNameLore: Boolean,
    itemStack: ItemStack
  ): MineStackObject[ItemStack] = {
    MineStackObject(mineStackObjectName, japaneseName, itemStack, hasNameLore, category)
  }

}
