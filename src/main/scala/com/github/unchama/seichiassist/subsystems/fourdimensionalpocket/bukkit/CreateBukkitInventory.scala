package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.bukkit

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.PocketSize
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.actions.CreateInventory
import org.bukkit.Bukkit
import org.bukkit.ChatColor.{BOLD, DARK_PURPLE}
import org.bukkit.inventory.Inventory

class CreateBukkitInventory[F[_]: Sync] extends CreateInventory[F, Inventory] {
  override def create(size: PocketSize): F[Inventory] =
    Sync[F].delay {
      Bukkit
        .getServer
        .createInventory(null, size.totalStackCount, s"$DARK_PURPLE${BOLD}4次元ポケット")
    }
}
