package com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject

import cats.effect.Sync
import com.github.unchama.minecraft.objects.{MinecraftItemStack, MinecraftMaterial}
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.CanBeSignedAsGachaPrize

case class MineStackObject[ItemStack](
  mineStackObjectName: String,
  uiName: Option[String],
  private val _itemStack: ItemStack,
  hasNameLore: Boolean,
  category: MineStackObjectCategory
) {

  import cats.implicits._

  def itemStack(implicit minecraftItemStack: MinecraftItemStack[ItemStack]): ItemStack =
    minecraftItemStack.copy(_itemStack)

  /**
   * 記名済みの[[ItemStack]]へ変換することを試みます
   */
  def tryToSignedItemStack[F[_]: Sync, Player](name: String)(
    implicit gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player],
    minecraftItemStack: MinecraftItemStack[ItemStack]
  ): F[Option[ItemStack]] = {
    if (category != MineStackObjectCategory.GACHA_PRIZES) return Sync[F].pure(None)

    implicit val canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
      gachaPrizeAPI.canBeSignedAsGachaPrize

    for {
      foundGachaPrize <- gachaPrizeAPI.findOfRegularPrizesByItemStack(itemStack)
    } yield foundGachaPrize.map { gachaPrize => gachaPrize.materializeWithOwnerSignature(name) }
  }

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
