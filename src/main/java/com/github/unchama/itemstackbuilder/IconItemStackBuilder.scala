package com.github.unchama.itemstackbuilder

import org.bukkit.Material

/**
 * ItemStack, 特にメニューに使用するスロットのIconを生成するBuilderです.
 *
 * @param material ItemStackに設定するMaterial
 * @param durability ダメージ値
 * Created by karayuu on 2019/03/30
 */
class IconItemStackBuilder(material: Material, durability: Short = 0.toShort()):
    AbstractItemStackBuilder<IconItemStackBuilder, ItemMeta>(material, durability) {
  private var showAttribute: Boolean = false

  /**
   * ItemStack(IconItemStackBuilder)の各種情報を表示させます.(シャベルの採掘速度等)
   *
   * @return このBuilder
   */
  def showAttribute(): IconItemStackBuilder {
    this.showAttribute = true
    return this
  }

  override def transformItemMetaOnBuild(meta: ItemMeta) {
    if (!showAttribute) {
      meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
    }
  }
}
