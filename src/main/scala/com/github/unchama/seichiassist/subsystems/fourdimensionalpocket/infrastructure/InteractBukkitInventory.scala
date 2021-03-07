package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.infrastructure

import cats.Monad
import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.PocketSize
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.actions.InteractInventory
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class InteractBukkitInventory[
  F[_] : Sync
] extends InteractInventory[F, Player, Inventory] {
  override def open(inventory: Inventory)(player: Player): F[Unit] =
    Sync[F].delay {
      player.openInventory(inventory)
    }

  import cats.implicits._

  import scala.jdk.CollectionConverters._

  override def extendSize(newSize: PocketSize)
                         (inventory: Inventory): F[Inventory] = {
    val shouldCreateNew: F[Boolean] = Sync[F].delay(inventory.getSize < newSize.totalStackCount)

    Monad[F].ifM(shouldCreateNew)(
      new CreateBukkitInventory[F].create(newSize).flatTap { newInventory =>
        Sync[F].delay {
          // 内容物をコピーする。サイズが上回っているため必ず格納ができる
          // TODO メインスレッドで閉じさせてアイテムを移行する
          inventory.asScala.zipWithIndex.foreach { case (stack, i) => newInventory.setItem(i, stack) }
        }
      },
      Monad[F].pure(inventory)
    )
  }

}
