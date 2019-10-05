package com.github.unchama.menuinventory

import cats.effect.IO
import com.github.unchama.targetedeffect.TargetedEffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder

/**
 * 共有された[sessionInventory]を作用付きの「メニュー」として扱うインベントリを保持するためのセッション.
 */
class MenuSession private[menuinventory](private var _view: MenuInventoryView) extends InventoryHolder {
  private val sessionInventory = _view.createConfiguredInventory(this)

  def view: MenuInventoryView = _view

  def overwriteViewWith(layout: IndexedSlotLayout): IO[Unit] = {
    _view = _view.copy(slotLayout = layout)

    view.slotLayout.setItemsOn(sessionInventory)
  }

  override def getInventory() = sessionInventory

  /**
   * このセッションが持つ共有インベントリを開く[TargetedEffect]を返します.
   */
  val openInventory: TargetedEffect[Player] =
    player => IO { player.openInventory(sessionInventory) }

}