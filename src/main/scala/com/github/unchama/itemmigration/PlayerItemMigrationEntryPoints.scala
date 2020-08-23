package com.github.unchama.itemmigration

import cats.effect.ConcurrentEffect
import com.github.unchama.itemmigration.controllers.player.{PlayerItemMigrationController, PlayerItemMigrationStateRepository}
import com.github.unchama.itemmigration.domain.ItemMigrations
import com.github.unchama.itemmigration.service.ItemMigrationService
import com.github.unchama.itemmigration.targets.PlayerInventoriesData

/**
 * プレーヤーのインベントリのマイグレーションを行うために必要なリスナー等のオブジェクトを提供するオブジェクトのクラス。
 */
class PlayerItemMigrationEntryPoints[F[_]](migrations: ItemMigrations,
                                           service: ItemMigrationService[F, PlayerInventoriesData[F]])
                                          (implicit F: ConcurrentEffect[F]) {

  private val repository = new PlayerItemMigrationStateRepository[F]
  private val controller = new PlayerItemMigrationController[F](repository, migrations, service)

  val listenersToBeRegistered = Seq(
    repository,
    controller
  )

}
