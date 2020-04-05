package com.github.unchama.playerdatarepository
import java.util.UUID

import cats.effect.{IO, SyncIO}
import org.bukkit.entity.Player

class NonPersistentPlayerDataRepository[D](initial: D) extends PlayerDataOnMemoryRepository[D] {
  override val loadData: (String, UUID) => SyncIO[Either[Option[String], D]] =
    (_, _) => SyncIO(Right(initial))

  override val unloadData: (Player, D) => IO[Unit] =
    (_, _) => IO.unit
}
