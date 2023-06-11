package com.github.unchama.seichiassist.subsystems.breakskilltargetconfig

import cats.data.Kleisli
import cats.effect.{Sync, SyncEffect}
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.application.repository.BreakSkillTargetConfigRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain.{
  BreakSkillTargetConfigKey,
  BreakSkillTargetConfigPersistence
}
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.persistence.JdbcBreakSkillTargetConfigPersistence
import org.bukkit.entity.Player

trait System[F[_], Player] extends Subsystem[F] {
  val api: BreakSkillTargetConfigAPI[F, Player]
}

object System {

  import cats.implicits._

  def wired[F[_]: Sync, G[_]: SyncEffect: ContextCoercion[*[_], F]]: G[System[F, Player]] = {
    implicit val breakSkillTargetConfigPersistence: BreakSkillTargetConfigPersistence[G] =
      new JdbcBreakSkillTargetConfigPersistence[G]

    for {
      breakSkillTargetConfigRepositoryControls <- BukkitRepositoryControls.createHandles(
        BreakSkillTargetConfigRepositoryDefinition.withContext[G, Player]
      )
    } yield {
      val breakSkillTargetConfigRepository = breakSkillTargetConfigRepositoryControls.repository

      new System[F, Player] {
        override val api: BreakSkillTargetConfigAPI[F, Player] =
          new BreakSkillTargetConfigAPI[F, Player] {
            override def toggleBreakSkillTargetConfig(
              configKey: BreakSkillTargetConfigKey
            ): Kleisli[F, Player, Unit] =
              Kleisli { player =>
                ContextCoercion(
                  breakSkillTargetConfigRepository(player)
                    .toggleBreakSkillTargetConfig(configKey)
                )
              }

            override def breakSkillTargetConfig(
              player: Player,
              configKey: BreakSkillTargetConfigKey
            ): F[Boolean] =
              ContextCoercion(
                breakSkillTargetConfigRepository(player).breakSkillTargetConfig(configKey)
              )
          }

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] = Seq(
          breakSkillTargetConfigRepositoryControls.coerceFinalizationContextTo[F]
        )
      }
    }
  }

}
