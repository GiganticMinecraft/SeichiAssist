package com.github.unchama.seichiassist.subsystems.shareinventory

import cats.effect.Sync
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.shareinventory.domain.bukkit.InventoryContents
import com.github.unchama.seichiassist.subsystems.shareinventory.infrastracture.JdbcShareInventoryPersistence

import java.util.UUID

trait System[F[_]] extends Subsystem[F] {
  val api: ShareInventoryAPI[F]
}

object System {

  def wired[F[_]: Sync]: System[F] = {
    val persistence = new JdbcShareInventoryPersistence[F]

    new System[F] {
      override implicit val api: ShareInventoryAPI[F] = new ShareInventoryAPI[F] {
        override def save(targetUuid: UUID, inventoryContents: InventoryContents): F[Unit] =
          persistence.saveSerializedShareInventory(targetUuid, inventoryContents)

        override def clear(targetUuid: UUID): F[Unit] =
          persistence.clearShareInventory(targetUuid)

        override def load(targetUuid: UUID): F[InventoryContents] =
          persistence.loadSerializedShareInventory(targetUuid)
      }
    }
  }

}
