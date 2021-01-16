package com.github.unchama.itemstackbuilder

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ItemMeta

import scala.collection.mutable

/**
 * ItemStack, 特にメニューに使用するスロットのIconを生成するBuilderです.
 *
 * @param material   ItemStackに設定するMaterial
 * @param durability ダメージ値
 *                   Created by karayuu on 2019/03/30
 */
class IconItemStackBuilder(material: Material, durability: Short = 0.toShort) extends
  AbstractItemStackBuilder[ItemMeta](material, durability) {
  private var shouldShowAttribute: Boolean = false

  /**
   * ItemStack(IconItemStackBuilder)の各種情報を表示させます.(シャベルの採掘速度等)
   *
   * @return このBuilder
   */
  def showAttribute(): this.type = {
    this.shouldShowAttribute = true
    this
  }

  override def transformItemMetaOnBuild(meta: ItemMeta): Unit = {
    super.transformItemMetaOnBuild(meta)
    if (!shouldShowAttribute) {
      meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
    }
  }
}
