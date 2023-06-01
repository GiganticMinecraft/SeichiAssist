package com.github.unchama.seichiassist.subsystems.breakskilltargetconfig

import cats.data.Kleisli
import cats.effect.{Sync, SyncEffect}
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.application.repository.BreakSkillTargetConfigRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain.{
  BreakSkillTargetConfig,
  BreakSkillTargetConfigKey,
  BreakSkillTargetConfigPersistence
}
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.persistence.JdbcBreakSkillTargetConfigPersistence
import org.bukkit.entity.Player

trait System[F[_], Player] extends Subsystem[F] {
  val api: BreakFlagAPI[F, Player]
}

object System {

  import cats.implicits._

  def wired[F[_]: Sync, G[_]: SyncEffect: ContextCoercion[*[_], F]]: G[System[F, Player]] = {
    implicit val breakFlagPersistence: BreakSkillTargetConfigPersistence[G] = new JdbcBreakSkillTargetConfigPersistence[G]

    for {
      breakFlagRepositoryControls <- BukkitRepositoryControls.createHandles(
        BreakSkillTargetConfigRepositoryDefinition.withContext[G, Player]
      )
    } yield {
      val breakFlagRepository = breakFlagRepositoryControls.repository

      new System[F, Player] {
        override val api: BreakFlagAPI[F, Player] = new BreakFlagAPI[F, Player] {
          override def toggleBreakFlag(
            breakFlagName: BreakSkillTargetConfigKey
          ): Kleisli[F, Player, Unit] =
            Kleisli { player =>
              for {
                breakFlag <- breakFlag(player, breakFlagName)
                _ <- ContextCoercion(breakFlagRepository(player).update { breakFlags =>
                  breakFlags.filterNot(_.configKey == breakFlagName) + BreakSkillTargetConfig(
                    breakFlagName,
                    includes = !breakFlag
                  )
                })
              } yield ()
            }

          override def breakFlag(
            player: Player,
            breakFlagName: BreakSkillTargetConfigKey
          ): F[Boolean] =
            ContextCoercion(for {
              flags <- breakFlagRepository(player).get
            } yield flags.find(_.configKey == breakFlagName).fold(true)(_.includes))
        }

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] = Seq(
          breakFlagRepositoryControls.coerceFinalizationContextTo[F]
        )
      }
    }
  }

}
