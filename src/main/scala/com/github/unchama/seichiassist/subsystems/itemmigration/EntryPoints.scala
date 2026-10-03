package com.github.unchama.seichiassist.subsystems.itemmigration

import cats.effect.{IO, Sync}
import com.github.unchama.generic.UnsafeSyncRunner

trait EntryPoints {

  def runDatabaseMigration[F[_]: Sync: UnsafeSyncRunner]: F[Unit]

  def runWorldMigration: IO[Unit]

}
