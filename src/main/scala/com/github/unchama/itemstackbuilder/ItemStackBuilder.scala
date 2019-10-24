package com.github.unchama.itemstackbuilder

import org.bukkit.inventory.{ItemFlag, ItemStack}

/**
 * [ItemStack] をBuildするBuilderを表すインターフェース.
 */
trait ItemStackBuilder {
  /**
   * [ItemStack] の表示名を設定します.
   *
   * @param title [ItemStack] の表示名
   * @return このBuilder
   */
  def title(title: String): ItemStackBuilder

  /**
   * [ItemStack] のLoreを設定します.
   *
   * @param lore [ItemStack] のLoreとして設定する [String] の [List]
   *             [List] に `null` が含まれていた場合,その行は無視されます.
   * @return このBuilder
   */
  def lore(lore: List[String]): ItemStackBuilder

  /**
   * [ItemStack] のLoreを設定します.
   *
   * @param _lore [ItemStack] のLoreとして設定する [String] の [List]
   *              [List] に `null` が含まれていた場合,その行は無視されます.
   * @return このBuilder
   */
  def lore(_lore: String*): ItemStackBuilder = lore(_lore.toList)

  /**
   * [ItemStack] にエンチャントを付与します.
   *
   * @return このBuilder
   */
  def enchanted(): ItemStackBuilder

  /**
   * [ItemStack] の個数を指定します.
   *
   * @param amount [ItemStack] の個数
   * @return このBuilder
   */
  def amount(amount: Int): ItemStackBuilder

  /**
   * [ItemStack] にunbreakableを付与する
   *
   * @return このBuilder
   */
  def unbreakable(): ItemStackBuilder

  /**
   * [ItemStack] に与えられた [ItemFlag] を付与する.
   *
   * @return このBuilder
   */
  def flagged(flag: ItemFlag): ItemStackBuilder

  /**
   * [ItemStack] に与えられた全ての [ItemFlag] を付与する.
   *
   * @return このBuilder
   */
  def flagged(flags: ItemFlag*): ItemStackBuilder = flagged(flags.toSet)

  /**
   * [ItemStack] に [Set] で与えられた [ItemFlag] を全て付与する.
   *
   * @return このBuilder
   */
  def flagged(flagSet: Set[ItemFlag]): ItemStackBuilder = flagSet.foldLeft(this) { case (acc, flag) => acc.flagged(flag) }

  /**
   * Builderによって指定された各引数を用いて [ItemStack] を生成します
   *
   * @return 生成された [ItemStack]
   */
  def build(): ItemStack
}
