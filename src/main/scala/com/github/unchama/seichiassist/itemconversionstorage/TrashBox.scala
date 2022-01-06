package com.github.unchama.seichiassist.itemconversionstorage

import cats.effect.IO
import cats.kernel.Monoid
import com.github.unchama.itemconversionstorage.{ConversionResultSet, ItemConversionStorage}
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.menuinventory.{MenuFrame, MenuSlotLayout}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * ゴミ箱
 */
object TrashBox extends ItemConversionStorage {
  override type Environment = ()
  override type ResultSet = ConversionResultSet.Plane

  override val frame: MenuFrame = MenuFrame(4.chestRows, "ゴミ箱")

  /**
   * ゴミ箱であるため、変換不要、及び返却不要。そのままアドレスの彼方へと葬り去る。
   * @return `player`からメニューの[[MenuSlotLayout]]を計算する[[IO]]
   */
  override def doOperation(player: Player, inventory: Map[Int, ItemStack])(implicit environment: Environment): IO[ResultSet] = IO.pure(summonMonoid.empty)

  override def doMap(player: Player, itemStack: ItemStack)(implicit environment: Environment): IO[ResultSet] = IO.pure(summonMonoid.empty)

  override protected implicit def summonMonoid: Monoid[ResultSet] = implicitly
}
