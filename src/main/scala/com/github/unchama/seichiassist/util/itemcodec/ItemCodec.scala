package com.github.unchama.seichiassist.util.itemcodec

import org.bukkit.inventory.ItemStack

/**
 * 予め定義が確定しているアイテムに対して適用可能なコーデックを定義するI/F。
 * アイテムには所有者など、「付加情報」が付属している場合がある。このI/Fまたそのサブクラスでは、その「付加情報」のことを
 * 「プロパティ」と呼ぶ。
 * @tparam P 対象のアイテムに関連するプロパティの型
 */
trait ItemCodec[P] {
  /**
   * ItemStackからプロパティを得ることを試みる。
   * @param from 対象
   * @return 有効なプロパティならSome、プロパティが取得できなかった場合はNone
   */
  def getProperty(from: ItemStack): Option[P]

  /**
   * このコーデックが指定した[[ItemStack]]を受け付けるかどうかチェックする。
   * 注意: デフォルトの実装では、[[getProperty]]が[[Some]]を返すことと、このメソッドが`true`を返すことは同値である。
   * @param stackForCheck チェックするItemStack
   * @return 受け付けるならtrue
   */
  def isAcceptable(stackForCheck: ItemStack): Boolean = getProperty(stackForCheck).nonEmpty

  /**
   * 指定したプロパティをItemStackへとデコードする。注意: 呼び出し側は返されたItemStackを自由に変更することができる。
   * @param property 対象のプロパティ
   * @return
   */
  def create(property: P): ItemStack
}
