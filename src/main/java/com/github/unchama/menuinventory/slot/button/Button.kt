package com.github.unchama.menuinventory.slot.button

import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.menuinventory.slot.button.action.ButtonEffect
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.asTargeted
import com.github.unchama.targetedeffect.ops.asSequentialEffect
import com.github.unchama.targetedeffect.ops.plus
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * メニューインベントリ上で「ボタン」として動作する[Slot]のクラス.
 *
 * [effects]により与えられる作用をリストの順で一つづつ実行していきます.
 *
 * [computeEffectOn]は常に与えられた[InventoryClickEvent]をキャンセルする副作用を含みます.
 *
 * @param itemStack  [Inventory] へセットする [ItemStack]
 * @author karayuu
 */
class Button(override val itemStack: ItemStack,
             private val effects: List<ButtonEffect>) : Slot {

  /**
   * [effects]をひとつずつ作用として発生させる [Slot] を構築します.
   */
  constructor(itemStack: ItemStack, vararg effects: ButtonEffect): this(itemStack, effects.toList())

  override fun computeEffectOn(event: InventoryClickEvent): TargetedEffect<Player> =
      asTargeted { event.isCancelled = true }
          .plus(
              this.effects.map { it.runAsyncEffectOn(event) }.asSequentialEffect()
          )

}
