package com.github.unchama.menuinventory

import cats.effect.IO
import cats.effect.concurrent.Ref
import com.github.unchama.targetedeffect.TargetedEffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.inventory.{Inventory, InventoryHolder}

/**
 * 共有された[sessionInventory]を作用付きの「メニュー」として扱うインベントリを保持するためのセッション.
 */
class MenuSession private[menuinventory](private val frame: InventoryFrame) extends InventoryHolder {

  private val sessionInventory = frame.createConfiguredInventory(this)

  val currentLayout: Ref[IO, IndexedSlotLayout] = Ref.unsafe(IndexedSlotLayout())

  def overwriteViewWith(layout: IndexedSlotLayout): IO[Unit] = {
    import cats.implicits._

    layout.setItemsOn(sessionInventory) *> currentLayout.set(layout)
  }

  override def getInventory: Inventory = sessionInventory

  /**
   * このセッションが持つ共有インベントリを開く[TargetedEffect]を返します.
   */
  val openInventory: TargetedEffect[Player] =
    player => IO { player.openInventory(sessionInventory) }

}
