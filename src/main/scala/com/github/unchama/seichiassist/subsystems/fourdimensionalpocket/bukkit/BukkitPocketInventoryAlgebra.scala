package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.bukkit

import cats.Monad
import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.{PocketInventoryAlgebra, PocketSize}
import org.bukkit.Bukkit
import org.bukkit.ChatColor.{BOLD, DARK_PURPLE}
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class BukkitPocketInventoryAlgebra[F[_] : Sync] extends PocketInventoryAlgebra[F, Player, Inventory] {
  override def open(inventory: Inventory)(player: Player): F[Unit] =
    Sync[F].delay {
      player.openInventory(inventory)
    }

  import cats.implicits._

  import scala.jdk.CollectionConverters._

  override def create(size: PocketSize): F[Inventory] =
    Sync[F].delay {
      Bukkit
        .getServer
        .createInventory(null, size.totalStackCount, s"$DARK_PURPLE${BOLD}4次元ポケット")
    }

  override def extendSize(newSize: PocketSize)
                         (inventory: Inventory): F[Inventory] = {
    val shouldCreateNew: F[Boolean] = Sync[F].delay(inventory.getSize < newSize.totalStackCount)

    Monad[F].ifM(shouldCreateNew)(
      create(newSize).flatTap { newInventory =>
        Sync[F].delay {
          // 内容物をコピーする。サイズが上回っているため必ず格納ができる
          inventory.asScala.zipWithIndex.foreach { case (stack, i) => newInventory.setItem(i, stack) }
        }
      },
      Monad[F].pure(inventory)
    )
  }

}
