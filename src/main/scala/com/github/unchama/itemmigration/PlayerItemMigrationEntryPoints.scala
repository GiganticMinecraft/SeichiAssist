package com.github.unchama.itemmigration

import cats.effect.{Concurrent, IO}
import com.github.unchama.itemmigration.controllers.player.{PlayerItemMigrationController, PlayerItemMigrationStateRepository}
import com.github.unchama.itemmigration.domain.{ItemMigrations, VersionedItemMigrationExecutor}
import com.github.unchama.itemmigration.targets.PlayerInventoriesData

class PlayerItemMigrationEntryPoints(migrations: ItemMigrations,
                                     executor: VersionedItemMigrationExecutor[IO, PlayerInventoriesData])
                                    (implicit concurrentIO: Concurrent[IO]) {

  private val repository = new PlayerItemMigrationStateRepository(migrations, executor)
  private val controller = new PlayerItemMigrationController(repository)

  val listenersToBeRegistered = Seq(
    repository,
    controller
  )

}
