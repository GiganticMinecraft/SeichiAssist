package com.github.unchama.menuinventory.slot.button

import cats.data
import cats.effect.IO
import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.menuinventory.slot.button.action.ButtonEffect
import com.github.unchama.targetedeffect.{TargetedEffect, UnfocusedEffect, _}
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

/**
 * メニューインベントリ上で「ボタン」として動作する[Slot]のクラス.
 *
 * [effects]により与えられる作用をリストの順で一つづつ実行していきます.
 *
 * [effectOn]は常に与えられた[InventoryClickEvent]をキャンセルする副作用を含みます.
 *
 * @param itemStack [Inventory] へセットする [ItemStack]
 * @author karayuu
 */
case class Button(override val itemStack: ItemStack,
                  private val effects: List[ButtonEffect]) extends Slot {
  override def effectOn(event: InventoryClickEvent): TargetedEffect[Player] = {
    import com.github.unchama.generic.syntax._
    import syntax._

    UnfocusedEffect {
      event.setCancelled(true)
    }.followedBy(data.Kleisli { t =>
      this.effects
        .map(_.asyncEffectOn(event))
        .asSequentialEffect()(t)
        .runAsync {
          case Left(error) => IO {
            println("Buttonの副作用の非同期実行中にエラーが発生しました.")
            error.printStackTrace()
          }
          case Right(_) => IO.pure(())
        }
        .toIO
    })
  }

  def withAnotherEffect(effect: ButtonEffect): Button = this.copy(effects = effects.appended(effect))
}

case object Button {
  /**
   * [effects]をひとつずつ作用として発生させる [Slot] を構築します.
   */
  def apply(itemStack: ItemStack, effects: ButtonEffect*): Button = Button(itemStack, effects.toList)

  val empty: Button = apply(new ItemStack(Material.AIR))
}