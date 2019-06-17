package com.github.unchama.menuinventory

import arrow.core.Either
import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.seichiassist.Schedulers
import com.github.unchama.targetedeffect.EmptyEffect
import com.github.unchama.targetedeffect.TargetedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

/**
 * インベントリのサイズを表すデータ型.
 * [Either.Left] にある値はインベントリのスロット数を表し, [Either.Right] にある値は [InventoryType] のいずれかとなる.
 */
typealias InventorySize = Either<Int, InventoryType>

/**
 * 入っているアイテムスタックをクリックすることで作用が引き起こされるような
 * インベントリのイミュータブルなビューを表すオブジェクトのクラス.
 *
 * @param size インベントリのサイズを決定するデータ
 * @param title インベントリのタイトル
 * @param slotMap インベントリの各スロットのindexと[Slot]間のMap
 */
class MenuInventoryView(private val size: InventorySize,
                        private val title: String,
                        private val slotMap: Map<Int, Slot>) : InventoryHolder {

  /**
   * このビューの[position]に登録された, 非同期スレッドから発火されてよい副作用を返す.
   */
  internal fun getAsyncEffectTriggerAt(position: Int): suspend (InventoryClickEvent) -> TargetedEffect<Player> = { event ->
    slotMap[position]?.computeEffectOn(event) ?: EmptyEffect
  }

  override fun getInventory(): Inventory {
    val configuredInventory = createInventory(size, title)

    CoroutineScope(Schedulers.async).launch {
      for (i in 0 until configuredInventory.size) {
        val slot = slotMap[i]
        if (slot != null) async { configuredInventory.setItem(i, slot.itemStack) }
      }
    }

    return configuredInventory
  }

  /**
   * 与えられたプロパティを用いて[Inventory] を作成します。
   *
   * [InventoryType.CHEST] の [Inventory] を作成する場合は [Either.Left] に作成する [Inventory] の大きさを、
   * それ以外の [Inventory] を作成する場合は [Either.Right] に [InventoryType] を入れてください。
   */
  private fun createInventory(property: InventorySize, title: String): Inventory =
      when (property) {
        is Either.Left -> Bukkit.createInventory(this, property.a, title)
        is Either.Right -> Bukkit.createInventory(this, property.b, title)
      }
}
