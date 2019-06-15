package com.github.unchama.menuinventory.itemstackbuilder

import org.bukkit.inventory.ItemStack

/**
 * [ItemStack] をBuildするBuilderを表すインターフェース.
 */
interface ItemStackBuilder {
  /**
   * [ItemStack] の表示名を設定します.
   *
   * @param title [ItemStack] の表示名
   * @return このBuilder
   */
  fun title(title: String): ItemStackBuilder

  /**
   * [ItemStack] のLoreを設定します.
   *
   * @param lore [ItemStack] のLoreとして設定する [String] の [List]
   * [List] に `null` が含まれていた場合,その行は無視されます.
   * @return このBuilder
   */
  fun lore(lore: List<String>): ItemStackBuilder

  /**
   * [ItemStack] にエンチャントを付与します.
   *
   * @return このBuilder
   */
  fun enchanted(): ItemStackBuilder

  /**
   * [ItemStack] の個数を指定します.
   *
   * @param amount [ItemStack] の個数
   * @return このBuilder
   */
  fun amount(amount: Int): ItemStackBuilder

  /**
   * Builderによって指定された各引数を用いて [ItemStack] を生成します
   *
   * @return 生成された [ItemStack]
   */
  fun build(): ItemStack
}
