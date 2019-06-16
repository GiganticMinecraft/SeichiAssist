package com.github.unchama.menuinventory.itemstackbuilder

import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

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
  fun showAttribute(): IconItemStackBuilder {
    this.showAttribute = true
    return this
  }

  override fun transformItemMetaOnBuild(meta: ItemMeta) {
    if (!showAttribute) {
      meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
    }
  }
}
