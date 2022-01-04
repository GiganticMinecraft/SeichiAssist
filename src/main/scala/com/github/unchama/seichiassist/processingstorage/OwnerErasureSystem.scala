package com.github.unchama.seichiassist.processingstorage

import cats.effect.IO
import com.github.unchama.menuinventory.MenuFrame
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.processingstorage.ProcessingStorage
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.ChatColor._

/**
 * 名義除去システム
 */
object OwnerErasureSystem extends ProcessingStorage {
  override type Environment = ()
  override val frame: MenuFrame = MenuFrame(4.chestRows, s"$GOLD${BOLD}所有者表記をなくしたいアイテムを投入してネ")

  /**
   * インベントリを閉じたときに発火される作用。
   *
   * @return `inventory`から変換されたアイテムのリストを計算する[[IO]]
   */
  override def doOperation(player: Player, inventory: Map[Int, ItemStack])(implicit environment: Environment): IO[List[ItemStack]] = IO {
    ???
  }

  /**
   *
   * @param itemStack 変換する前のアイテム
   * @return 返す対象である場合変換後の[[ItemStack]]を包んだ[[Some]]、返さない場合[[None]]を返す[[IO]]。
   */
  override def doMap(itemStack: ItemStack): IO[Option[ItemStack]] = IO {
    ???
  }
}
