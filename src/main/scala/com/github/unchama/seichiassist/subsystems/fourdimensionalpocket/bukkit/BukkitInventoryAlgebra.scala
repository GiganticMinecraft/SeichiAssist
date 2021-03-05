package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.bukkit

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.{InventoryAlgebra, PocketSize}
import org.bukkit.Bukkit
import org.bukkit.ChatColor.{BOLD, DARK_PURPLE}
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class BukkitInventoryAlgebra[F[_] : Sync] extends InventoryAlgebra[F, Player, Inventory] {
  override def open(inventory: Inventory)(player: Player): F[Unit] =
    Sync[F].delay {
      player.openInventory(inventory)
    }

  import scala.jdk.CollectionConverters._
  import scala.util.chaining._

  override def extendSize(newSize: PocketSize)
                         (inventory: Inventory): F[Inventory] =
    Sync[F].delay {
      if (inventory.getSize < newSize.totalStackCount)
        Bukkit
          .getServer
          .createInventory(null, newSize.totalStackCount, s"$DARK_PURPLE${BOLD}4次元ポケット")
          .tap { newInventory =>
            // 内容物をコピーする
            // サイズが上回っているため必ず格納ができる
            inventory.asScala.zipWithIndex
              .foreach { case (stack, i) =>
                newInventory.setItem(i, stack)
              }
          }
      else inventory
    }

}
