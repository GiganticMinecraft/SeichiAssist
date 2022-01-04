package com.github.unchama.seichiassist.itemconversionstorage

import cats.effect.IO
import com.github.unchama.itemconversionstorage.ItemConversionStorage
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.menuinventory.{MenuFrame, MenuSlotLayout}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * ゴミ箱
 */
object TrashBox extends ItemConversionStorage {
  override type Environment = ()
  override val frame: MenuFrame = MenuFrame(4.chestRows, "ゴミ箱")

  /**
   * ゴミ箱であるため、変換不要、及び返却不要。そのままアドレスの彼方へと葬り去る。
   * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def doOperation(player: Player, inventory: Map[Int, ItemStack])(implicit environment: this.Environment): IO[List[ItemStack]] = IO.pure(List.empty)

  override def doMap(itemStack: ItemStack): IO[Option[ItemStack]] = IO.pure(None)
}
