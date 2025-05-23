package com.github.unchama.itemstackbuilder

import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.{PotionData, PotionType}

class TippedArrowIconItemStackBuilder(val potionData: PotionData)
    extends AbstractItemStackBuilder[PotionMeta](Material.TIPPED_ARROW) {

  def this(potionType: PotionType) = this(new PotionData(potionType))

  /**
   * 生成されるアイテムスタックに入る[ItemMeta]を, ビルダー内の情報に基づいて変更する.
   */
  override protected def transformItemMetaOnBuild(meta: PotionMeta): Unit = {
    meta.setBasePotionData(potionData)
    meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
  }
}
