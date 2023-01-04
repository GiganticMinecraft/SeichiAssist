package com.github.unchama.seichiassist.subsystems.sharedinventory

import cats.effect.{Concurrent, ConcurrentEffect, Timer}
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.sharedinventory.bukkit.command.ShareInventoryCommand
import com.github.unchama.seichiassist.subsystems.sharedinventory.domain.bukkit.InventoryContents
import com.github.unchama.seichiassist.subsystems.sharedinventory.domain.{
  SharedFlag,
  SharedInventoryPersistence,
  SharedInventoryUsageSemaphore
}
import com.github.unchama.seichiassist.subsystems.sharedinventory.infrastracture.JdbcSharedInventoryPersistence
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

import java.util.UUID

trait System[F[_]] extends Subsystem[F] {
  val api: SharedInventoryAPI[F, Player]
}

object System {

  import cats.implicits._

  def wired[F[_]: ConcurrentEffect: Timer, G[_]: Concurrent]: G[System[F]] = {
    implicit val persistence: SharedInventoryPersistence[F] =
      new JdbcSharedInventoryPersistence[F]

    for {
      usageSemaphore <- SharedInventoryUsageSemaphore.newIn[F, G]
    } yield {
      new System[F] {
        override implicit val api: SharedInventoryAPI[F, Player] =
          new SharedInventoryAPI[F, Player] {
            override def save(targetUuid: UUID, inventoryContents: InventoryContents): F[Unit] =
              usageSemaphore.trySaveTransaction(targetUuid, inventoryContents)

            override def clear(targetUuid: UUID): F[Unit] =
              usageSemaphore.tryClearTransaction(targetUuid)

            override def load(targetUuid: UUID): F[Option[InventoryContents]] =
              persistence.load(targetUuid)

            override def sharedFlag(player: Player): F[SharedFlag] =
              load(player.getUniqueId)
                .map(_.fold[SharedFlag](SharedFlag.NotSharing)(_ => SharedFlag.Sharing))

          }
        override val commands: Map[String, TabExecutor] = {
          Map("shareinv" -> new ShareInventoryCommand[F].executor)
        }
      }
    }
  }

}
