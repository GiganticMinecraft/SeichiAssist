package com.github.unchama.playerdatarepository
import java.util.UUID

import cats.effect.concurrent.Ref
import cats.effect.{IO, SyncIO}
import org.bukkit.entity.Player

class NonPersistentPlayerDataRefRepository[D](initial: D) extends PreLoginToQuitPlayerDataRepository[Ref[IO, D]] {
  override val loadData: (String, UUID) => SyncIO[Either[Option[String], Ref[IO, D]]] =
    (_, _) => Ref.in[SyncIO, IO, D](initial).map(Right(_))

  override val unloadData: (Player, Ref[IO, D]) => IO[Unit] =
    (_, _) => IO.unit
}
