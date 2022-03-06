package com.github.unchama.itemmigration.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

class PlayerMigrationState[F[_]] private (flagRef: Ref[F, Boolean]) {

  val setMigrated: F[Unit] = flagRef.set(true)

  val hasMigrated: F[Boolean] = flagRef.get

}

object PlayerMigrationState {

  import cats.implicits._

  def newIn[F[_]: Sync]: F[PlayerMigrationState[F]] =
    for {
      ref <- Ref[F].of(false)
    } yield new PlayerMigrationState[F](ref)

}
