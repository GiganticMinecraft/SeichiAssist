package com.github.unchama.menuinventory

/**
 * 共有された[sessionInventory]を作用付きの「メニュー」として扱うインベントリを保持するためのセッション.
 */
class MenuSession private[menuinventory] (view: MenuInventoryView): InventoryHolder {
  private val sessionInventory = view.createConfiguredInventory(this)

  var view: MenuInventoryView = view
    private set

  internal suspend def overwriteViewWith(layout: IndexedSlotLayout) {
    view = view.copy(slotLayout = layout)
    view.slotLayout.asynchronouslySetItemsOn(sessionInventory)
  }

  override def getInventory() = sessionInventory

  /**
   * このセッションが持つ共有インベントリを開く[TargetedEffect]を返します.
   *
   * @param syncExecutionContext インベントリを開くコルーチンの実行コンテキスト
   */
  def openEffectThrough(syncExecutionContext: CoroutineContext): TargetedEffect<Player> = TargetedEffect {
    withContext(syncExecutionContext) {
      it.openInventory(sessionInventory)
    }
  }

}