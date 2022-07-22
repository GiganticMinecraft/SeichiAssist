package com.github.unchama.seichiassist.subsystems.shareinventory

import cats.effect.ConcurrentEffect
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.shareinventory.bukkit.command.ShareInventoryCommand
import com.github.unchama.seichiassist.subsystems.shareinventory.domain.bukkit.InventoryContents
import com.github.unchama.seichiassist.subsystems.shareinventory.infrastracture.JdbcSharedInventoryPersistence
import org.bukkit.command.TabExecutor

import java.util.UUID

trait System[F[_]] extends Subsystem[F] {
  val api: SharedInventoryAPI[F]
}

object System {

  def wired[F[_]: ConcurrentEffect]: System[F] = {
    val persistence = new JdbcSharedInventoryPersistence[F]

    new System[F] {
      override implicit val api: SharedInventoryAPI[F] = new SharedInventoryAPI[F] {
        override def save(targetUuid: UUID, inventoryContents: InventoryContents): F[Unit] =
          persistence.save(targetUuid, inventoryContents)

        override def clear(targetUuid: UUID): F[Unit] =
          persistence.clear(targetUuid)

        override def load(targetUuid: UUID): F[Option[InventoryContents]] =
          persistence.load(targetUuid)
      }

      override val commands: Map[String, TabExecutor] = Map(
        "shareinv" -> new ShareInventoryCommand[F].executor
      )
    }
  }

}
