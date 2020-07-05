package com.github.unchama.itemmigration.template

import cats.effect.{CancelToken, IO}
import org.bukkit.entity.Player

trait PlayerItemMigrationFiber {
  def invokeWith(player: Player): IO[Unit]

  def isComplete: IO[Boolean]

  val cancel: CancelToken[IO]
}
