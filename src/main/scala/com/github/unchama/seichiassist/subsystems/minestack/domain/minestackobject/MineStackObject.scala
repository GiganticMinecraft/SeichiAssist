package com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject

import com.github.unchama.minecraft.objects.{MinecraftItemStack, MinecraftMaterial}
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  GachaPrize,
  GachaPrizeId,
  GachaProbability
}

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

  /**
   * [[GachaPrize]]への変換を試みる。
   * これは「[[MineStackObjectCategory]]がGachaPrizesであるならば、
   * [[GachaPrize]]に変換することが可能である」という条件を前提としている。
   *
   * FIXME: この実装は[[GachaProbability]]が0.0(つまり通常では排出されない)
   *  になっていたり、[[GachaPrizeId]]が[[Int]]のMaxValueを取っていたりしているが、
   *  [[GachaProbability]]は今後の処理に一切影響を与えないという
   *  前提があり(これが直接ガチャで排出されるわけではないので)このようにしている。
   *  また、[[GachaPrizeId]]でIntのMaxValueを取っているのは、[[GachaPrizeId]]が[[GachaPrize]]のIdと
   *  重複しないようにしているためである。
   *  そしてこの実装は、ガチャシステム側で通常排出アイテムと
   *  イベント時に排出されるアイテムが常にSeichiAssist側で取得できる機構
   *  see: https://github.com/GiganticMinecraft/SeichiAssist/issues/1657
   *  を実装した後に、そちら側に合わせる実装にするべきである。
   *  ※現在イベント時に期間限定で排出されるGTアイテムなどを登録する際に、
   *  一度通常GTアイテムを削除しているようなので、現在の仕様のままガチャシステムから[[GachaPrize]]を取得
   *  しようとすると期間限定GTアイテムが排出されている期間は、MineStackからGTアイテムを取り出すことができない
   *  (期間中は[[GachaPrize]]が存在しないので)ということになるのを防ぐため、確実に[[GachaPrize]]を取得するために
   *  やむを得ず実装したものである。
   */
  def tryToGachaPrize: Option[GachaPrize[ItemStack]] = {
    if (
      category != MineStackObjectCategory.GACHA_PRIZES || category == MineStackObjectCategory.BUILTIN_GACHA_PRIZES
    )
      return None
    Some(
      GachaPrize[ItemStack](
        _itemStack,
        GachaProbability(0.0),
        signOwner = true,
        GachaPrizeId(Int.MaxValue)
      )
    )
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
