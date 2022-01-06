package com.github.unchama.seichiassist.itemconversionstorage

import cats.effect.IO
import com.github.unchama.itemconversionstorage.{ConversionResultSet, ItemConversionStorage}
import com.github.unchama.menuinventory.MenuFrame
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * GT --> 椎名林檎
 */
object GiganticTrade extends ItemConversionStorage {
  override type Environment = ()
  override val frame: MenuFrame = MenuFrame(4.chestRows, s"${GOLD.toString}${BOLD}椎名林檎と交換したい景品を入れてネ")

  /**
   * インベントリを閉じたときに発火される作用。
   *
   * @return `inventory`から変換されたアイテムのリストを計算する[[IO]]
   */
  override def doOperation(player: Player, inventory: Map[Int, ItemStack])(implicit environment: GiganticTrade): IO[ConversionResultSet] = ???

  /**
   *
   * @param itemStack 変換する前のアイテム
   * @return 返す対象である場合変換後の[[ItemStack]]を包んだ[[Some]]、返さない場合[[None]]を返す[[IO]]。
   */
override def doMap(player: Player, itemStack: ItemStack): IO[ConversionResultSet] = ???
}
