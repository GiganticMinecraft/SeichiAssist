package com.github.unchama.itemmigration.application

import cats.Applicative
import cats.effect.Sync
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.SinglePhasedRepositoryInitialization
import com.github.unchama.itemmigration.domain.PlayerMigrationState

object ItemMigrationStateRepositoryDefinitions {

  def initialization[F[_]: Sync]
    : SinglePhasedRepositoryInitialization[F, PlayerMigrationState[F]] =
    SinglePhasedRepositoryInitialization.withSupplier(PlayerMigrationState.newIn[F])

  def finalization[F[_]: Applicative, Player]
    : RepositoryFinalization[F, Player, PlayerMigrationState[F]] =
    RepositoryFinalization.trivial

}
