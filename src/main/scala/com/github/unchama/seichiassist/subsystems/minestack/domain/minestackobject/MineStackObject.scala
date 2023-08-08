package com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject

import cats.effect.Sync
import com.github.unchama.generic.Cloneable
import com.github.unchama.minecraft.objects.MinecraftMaterial
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.CanBeSignedAsGachaPrize

case class MineStackObject[ItemStack: Cloneable](
  mineStackObjectName: String,
  uiName: Option[String],
  private val _itemStack: ItemStack,
  hasNameLore: Boolean,
  category: MineStackObjectCategory
) {

  import cats.implicits._

  def itemStack: ItemStack = Cloneable[ItemStack].clone(_itemStack)

  /**
   * [[MineStackObject]]は[[MineStackObjectCategory]]が[[MineStackObjectCategory.GACHA_PRIZES]]のアイテムとして
   * [[com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaPrize]]の
   * 通常排出アイテム(`_.nonGachaEventItem`)を含む。
   *
   * また、[[MineStackObjectCategory.GACHA_PRIZES]]は記名する事ができるアイテムを持つという性質があるが、
   * [[MineStackObject]]自体にはその性質がないため、[[MineStackObject]]の通常排出アイテムの中に
   * [[itemStack]]と同じアイテムが存在するならば、`name`を所有者として記名した[[ItemStack]]を返す。
   *
   * @return 記名済みの[[ItemStack]]へ変換することを試みる作用
   */
  def tryToSignedItemStack[F[_]: Sync, Player](
    name: String
  )(implicit gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player]): F[Option[ItemStack]] = {
    if (category != MineStackObjectCategory.GACHA_PRIZES) return Sync[F].pure(None)

    implicit val canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
      gachaPrizeAPI.canBeSignedAsGachaPrize

    for {
      gachaPrizes <- gachaPrizeAPI.allGachaPrizeList
    } yield gachaPrizes.filter(_.nonGachaEventItem).find(_.itemStack == itemStack).map {
      gachaPrize => gachaPrize.materializeWithOwnerSignature(name)
    }
  }

}

object MineStackObject {

  def MineStackObjectByMaterial[ItemStack: Cloneable, Material](
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

  def MineStackObjectByItemStack[ItemStack: Cloneable](
    category: MineStackObjectCategory,
    mineStackObjectName: String,
    japaneseName: Option[String],
    hasNameLore: Boolean,
    itemStack: ItemStack
  ): MineStackObject[ItemStack] = {
    MineStackObject(mineStackObjectName, japaneseName, itemStack, hasNameLore, category)
  }

}
