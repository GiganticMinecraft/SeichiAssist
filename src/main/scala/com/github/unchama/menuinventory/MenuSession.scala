package com.github.unchama.menuinventory

import cats.Eq
import cats.effect.concurrent.Ref
import cats.effect.{ContextShift, IO}
import com.github.unchama.menuinventory.Types.LayoutPreparationContext
import com.github.unchama.menuinventory.slot.Slot
import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.{Inventory, InventoryHolder, ItemStack}

/**
 * 共有された[sessionInventory]を作用付きの「メニュー」として扱うインベントリを保持するためのセッション.
 */
class MenuSession private[menuinventory](private val frame: MenuFrame) extends InventoryHolder {

  val currentLayout: Ref[IO, MenuSlotLayout] = Ref.unsafe(MenuSlotLayout())

  private val sessionInventory = frame.createConfiguredInventory(this)

  /**
   * このセッションが持つ共有インベントリを開く[TargetedEffect]を返します.
   */
  val openInventory: TargetedEffect[Player] =
    targetedeffect.delay { player =>
      player.openInventory(sessionInventory)
    }

  def overwriteViewWith(newLayout: MenuSlotLayout)(implicit ctx: LayoutPreparationContext): IO[Unit] = {
    type LayoutDiff = Map[Int, Option[Slot]]

    // 差分があるインデックスを列挙する
    def differences(oldLayout: MenuSlotLayout, newLayout: MenuSlotLayout): LayoutDiff = {
      def mapDifferences[K, V: Eq](oldMap: Map[K, V], newMap: Map[K, V]): Map[K, Option[V]] = {
        val domain = oldMap.keySet.union(newMap.keySet)

        import cats.implicits._

        domain
          .map(key => key -> newMap.get(key))
          .toMap
          .filter { case (key, newValue) => oldMap.get(key) neqv newValue }
      }

      implicit val slotEq: Eq[Slot] = (x: Slot, y: Slot) => x eq y

      mapDifferences(oldLayout.layoutMap, newLayout.layoutMap)
    }

    def updateMenuSlots(updates: LayoutDiff): IO[Unit] = {
      import cats.implicits._

      val effects = for {
        (slotIndex, slotOption) <- updates.toList
        itemStack = slotOption.map(_.itemStack).getOrElse(new ItemStack(Material.AIR))
      } yield IO { sessionInventory.setItem(slotIndex, itemStack) }

      implicit val context: ContextShift[IO] = IO.contextShift(ctx)
      effects.parSequence_
    }

    for {
      oldLayout <- currentLayout.get
      diff = differences(oldLayout, newLayout)
      _ <- currentLayout.set(newLayout)
      _ <- updateMenuSlots(diff)
    } yield ()
  }

  override def getInventory: Inventory = sessionInventory

}
