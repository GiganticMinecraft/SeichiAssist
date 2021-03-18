package com.github.unchama.menuinventory.slot.button.action

import cats.effect.IO
import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.menuinventory.{LayoutPreparationContext, MenuSession, MenuSlotLayout}
import com.github.unchama.minecraft.actions.MinecraftServerThreadShift
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * [ButtonEffect]に渡すScoped Lambdaの中で実行可能であるべきメソッドを提供するスコープオブジェクトのクラス.
 */
case class ButtonEffectScope(event: InventoryClickEvent) {
  def overwriteCurrentViewBy(newLayout: MenuSlotLayout)
                            (implicit ctx: LayoutPreparationContext,
                             syncShift: MinecraftServerThreadShift[IO]): IO[Unit] =
    event.getInventory.getHolder.asInstanceOf[MenuSession].overwriteViewWith(newLayout)

  def overwriteCurrentSlotBy(newSlot: Slot)
                            (implicit ctx: LayoutPreparationContext,
                             syncShift: MinecraftServerThreadShift[IO]): IO[Unit] = {
    val session = event.getInventory.getHolder.asInstanceOf[MenuSession]

    for {
      oldLayout <- session.currentLayout.get
      newLayout = oldLayout.altered(event.getSlot -> newSlot)
      _ <- session.overwriteViewWith(newLayout)
    } yield ()
  }
}
