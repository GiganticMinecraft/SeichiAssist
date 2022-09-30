package com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject

import com.github.unchama.minecraft.objects.MinecraftMaterial

case class MineStackObject[ItemStack <: Cloneable](
  mineStackObjectName: String,
  uiName: Option[String],
  private val _itemStack: ItemStack,
  hasNameLore: Boolean,
  category: MineStackObjectCategory
) {

  def itemStack: ItemStack = _itemStack.clone[ItemStack]

//  /**
//   * [[GachaPrize]]への変換した後、記名済みの[[ItemStack]]へ変換することを試みます
//   *
//   * FIXME: この実装は[[GachaProbability]]が0.0(つまり通常では排出されない)
//   *  になっていたり、[[GachaPrizeId]]が[[Int]]のMaxValueを取っていたりしているが、
//   *  [[GachaProbability]]は今後の処理に一切影響を与えないという
//   *  前提があり(これが直接ガチャで排出されるわけではないので)このようにしている。
//   *  また、[[GachaPrizeId]]でIntのMaxValueを取っているのは、[[GachaPrizeId]]が[[GachaPrize]]のIdと
//   *  重複しないようにしているためである。
//   *  そしてこの実装は、ガチャシステム側で通常排出アイテムと
//   *  イベント時に排出されるアイテムが常にSeichiAssist側で取得できる機構
//   *  see: https://github.com/GiganticMinecraft/SeichiAssist/issues/1657
//   *  を実装した後に、そちら側に合わせる実装にするべきである。
//   *  ※現在イベント時に期間限定で排出されるGTアイテムなどを登録する際に、
//   *  一度通常GTアイテムを削除しているようなので、現在の仕様のままガチャシステムから[[GachaPrize]]を取得
//   *  しようとすると期間限定GTアイテムが排出されている期間は、MineStackからGTアイテムを取り出すことができない
//   *  (期間中は[[GachaPrize]]が存在しないので)ということになるのを防ぐため、確実に[[GachaPrize]]を取得するために
//   *  やむを得ず実装したものである。
//   */
//  def tryToSignedItemStack(
//    name: String
//  )(implicit signedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack]): Option[ItemStack] = {
//    if (
//      category != MineStackObjectCategory.GACHA_PRIZES || category == MineStackObjectCategory.BUILTIN_GACHA_PRIZES
//    ) return None
//
//    val gachaPrize = GachaPrize[ItemStack](
//      _itemStack,
//      GachaProbability(0.0),
//      signOwner = true,
//      GachaPrizeId(Int.MaxValue)
//    )
//
//    Some(signedAsGachaPrize.signWith(name)(gachaPrize))
//  }

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
