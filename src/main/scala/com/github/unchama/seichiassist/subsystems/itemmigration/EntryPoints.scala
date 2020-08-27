package com.github.unchama.seichiassist.subsystems.itemmigration

import cats.effect.{IO, SyncEffect}

trait EntryPoints {

  def runDatabaseMigration[F[_] : SyncEffect]: F[Unit]

  def runWorldMigration: IO[Unit]

}
