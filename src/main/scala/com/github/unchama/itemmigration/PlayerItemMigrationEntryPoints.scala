package com.github.unchama.itemmigration

import cats.effect.{Concurrent, IO}
import com.github.unchama.itemmigration.controllers.player.{PlayerItemMigrationController, PlayerItemMigrationStateRepository}
import com.github.unchama.itemmigration.domain.{ItemMigrationVersionRepository, ItemMigrations}
import com.github.unchama.itemmigration.service.ItemMigrationService
import com.github.unchama.itemmigration.targets.PlayerInventoriesData

/**
 * プレーヤーのインベントリのマイグレーションを行うために必要なリスナー等のオブジェクトを提供するオブジェクトのクラス。
 */
class PlayerItemMigrationEntryPoints(migrations: ItemMigrations,
                                     service: ItemMigrationService[IO, PlayerInventoriesData])
                                    (implicit concurrentIO: Concurrent[IO]) {

  private val repository = new PlayerItemMigrationStateRepository(migrations, service)
  private val controller = new PlayerItemMigrationController(repository)

  val listenersToBeRegistered = Seq(
    repository,
    controller
  )

}
