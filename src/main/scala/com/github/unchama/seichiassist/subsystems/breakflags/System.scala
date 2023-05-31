package com.github.unchama.seichiassist.subsystems.breakflags

import cats.effect.{Sync, SyncEffect}
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakflags.application.repository.BreakFlagRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.breakflags.domain.BreakFlagPersistence
import com.github.unchama.seichiassist.subsystems.breakflags.persistence.JdbcBreakFlagPersistence

trait System[F[_]] extends Subsystem[F] {

}

object System {

  import cats.implicits._

  def wired[F[_]: SyncEffect]: F[System[F]] = {
    implicit val breakFlagPersistence: BreakFlagPersistence[F] = new JdbcBreakFlagPersistence[F]

    for {
      breakFlagRepositoryControls <- BukkitRepositoryControls.createHandles(
        BreakFlagRepositoryDefinition.withContext
      )
    } yield {
      val breakFlagRepository = breakFlagRepositoryControls.repository

    }
  }

}
