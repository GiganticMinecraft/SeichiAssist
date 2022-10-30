package com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject

import cats.effect.Sync
import com.github.unchama.minecraft.objects.MinecraftMaterial
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.domain.CanBeSignedAsGachaPrize

case class MineStackObject[ItemStack <: Cloneable](
  mineStackObjectName: String,
  uiName: Option[String],
  private val _itemStack: ItemStack,
  hasNameLore: Boolean,
  category: MineStackObjectCategory
) {

  def itemStack: ItemStack = _itemStack.clone[ItemStack]

  /**
   * 記名済みの[[ItemStack]]へ変換することを試みます
   */
  def tryToSignedItemStack[F[_]: Sync, Player](
    name: String
  )(implicit gachaAPI: GachaAPI[F, ItemStack, Player]): F[Option[ItemStack]] = {
    if (
      category != MineStackObjectCategory.GACHA_PRIZES || category == MineStackObjectCategory.BUILTIN_GACHA_PRIZES
    ) return Sync[F].pure(None)

    import cats.implicits._

    implicit val canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
      gachaAPI.canBeSignedAsGachaPrize

    for {
      foundGachaPrize <- gachaAPI.findByItemStack(itemStack)
    } yield foundGachaPrize.map { gachaPrize => gachaPrize.materializeWithOwnerSignature(name) }
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
