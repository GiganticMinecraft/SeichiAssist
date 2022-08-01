package com.github.unchama.seichiassist.subsystems.sharedinventory.domain

import cats.effect.{Concurrent, Sync, Timer}
import com.github.unchama.generic.effect.concurrent.RecoveringSemaphore
import com.github.unchama.seichiassist.subsystems.sharedinventory.domain.bukkit.InventoryContents

import java.util.UUID

class SharedInventoryUsageSemaphore[F[_]](recoveringSemaphore: RecoveringSemaphore[F])(
  implicit persistence: SharedInventoryPersistence[F]
) {

  def trySaveTransaction(uuid: UUID, inventoryContents: InventoryContents): F[Unit] =
    recoveringSemaphore.tryUse {
      persistence.save(uuid, inventoryContents)
    }(SharedInventoryUsageSemaphore.usageInterval)

  def tryClearTransaction(uuid: UUID): F[Unit] =
    recoveringSemaphore.tryUse {
      persistence.clear(uuid)
    }(SharedInventoryUsageSemaphore.usageInterval)

}

object SharedInventoryUsageSemaphore {

  import cats.implicits._

  import scala.concurrent.duration._

  /**
   * 再び共有インベントリを利用できるまでのクールタイム時間
   */
  final val usageInterval = 10.second

  def newIn[F[_]: Concurrent: Timer, G[_]: Sync](
    implicit persistence: SharedInventoryPersistence[F]
  ): G[SharedInventoryUsageSemaphore[F]] =
    RecoveringSemaphore.newIn[G, F].map(rs => new SharedInventoryUsageSemaphore[F](rs))

}
