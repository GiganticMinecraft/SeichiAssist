package com.github.unchama.seichiassist.subsystems.breakflags

import cats.data.Kleisli
import cats.effect.{Sync, SyncEffect}
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakflags.application.repository.BreakFlagRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.breakflags.domain.{
  BreakFlag,
  BreakFlagName,
  BreakFlagPersistence
}
import com.github.unchama.seichiassist.subsystems.breakflags.persistence.JdbcBreakFlagPersistence
import org.bukkit.entity.Player

trait System[F[_], Player] extends Subsystem[F] {
  val api: BreakFlagAPI[F, Player]
}

object System {

  import cats.implicits._

  def wired[F[_]: Sync, G[_]: SyncEffect: ContextCoercion[*[_], F]]: G[System[F, Player]] = {
    implicit val breakFlagPersistence: BreakFlagPersistence[G] = new JdbcBreakFlagPersistence[G]

    for {
      breakFlagRepositoryControls <- BukkitRepositoryControls.createHandles(
        BreakFlagRepositoryDefinition.withContext[G, Player]
      )
    } yield {
      val breakFlagRepository = breakFlagRepositoryControls.repository

      new System[F, Player] {
        override val api: BreakFlagAPI[F, Player] = new BreakFlagAPI[F, Player] {
          override def toggleBreakFlag(breakFlagName: BreakFlagName): Kleisli[F, Player, Unit] =
            Kleisli { player =>
              for {
                breakFlag <- breakFlag(player, breakFlagName)
                _ <- ContextCoercion(breakFlagRepository(player).update { breakFlags =>
                  breakFlags.filterNot(_.flagName == breakFlagName) :+ BreakFlag(
                    breakFlagName,
                    flag = !breakFlag
                  )
                })
              } yield ()
            }

          override def breakFlag(player: Player, breakFlagName: BreakFlagName): F[Boolean] =
            ContextCoercion(for {
              flags <- breakFlagRepository(player).get
            } yield {
              flags.find(_.flagName == breakFlagName) match {
                case Some(value) => value.flag
                case None        => true // 破壊フラグのデフォルト値はtrue(破壊する)
              }
            })
        }
      }
    }
  }

}
