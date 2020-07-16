package com.github.unchama.itemmigration

import cats.effect.{Concurrent, IO}
import com.github.unchama.itemmigration.controllers.player.{PlayerItemMigrationController, PlayerItemMigrationStateRepository}
import com.github.unchama.itemmigration.domain.{ItemMigrations, VersionedItemMigrationExecutor}
import org.bukkit.entity.Player

class PlayerItemMigrationEntryPoints(implicit migrations: ItemMigrations,
                                     executor: VersionedItemMigrationExecutor[IO, Player],
                                     concurrentIO: Concurrent[IO]) {

  private val repository = new PlayerItemMigrationStateRepository(migrations, executor)
  private val controller = new PlayerItemMigrationController(repository)

  val listenersToBeRegistered = Seq(
    repository,
    controller
  )

}
