package com.github.unchama.seichiassist.subsystems.breakskilltriggerconfig

import cats.data.Kleisli
import cats.effect.SyncEffect
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakskilltriggerconfig.application.repository.BreakSkillTriggerConfigRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.breakskilltriggerconfig.domain.{
  BreakSkillTriggerConfigKey,
  BreakSkillTriggerConfigPersistence
}
import com.github.unchama.seichiassist.subsystems.breakskilltriggerconfig.persistence.JdbcBreakSkillTriggerConfigPersistence
import org.bukkit.entity.Player

trait System[F[_], Player] extends Subsystem[F] {
  val api: BreakSkillTriggerConfigAPI[F, Player]
}

object System {

  import cats.implicits._

  def wired[F[_], G[_]: SyncEffect: ContextCoercion[*[_], F]]: G[System[F, Player]] = {
    implicit val breakSkillTriggerConfigPersistence: BreakSkillTriggerConfigPersistence[G] =
      new JdbcBreakSkillTriggerConfigPersistence[G]

    for {
      breakSkillTriggerConfigRepositoryControls <- BukkitRepositoryControls.createHandles(
        BreakSkillTriggerConfigRepositoryDefinition.withContext[G, Player]
      )
    } yield {
      val breakSkillTriggerConfigRepository = breakSkillTriggerConfigRepositoryControls.repository

      new System[F, Player] {
        override val api: BreakSkillTriggerConfigAPI[F, Player] =
          new BreakSkillTriggerConfigAPI[F, Player] {
            override def toggleBreakSkillTriggerConfig(
              configKey: BreakSkillTriggerConfigKey
            ): Kleisli[F, Player, Unit] =
              Kleisli { player =>
                ContextCoercion(
                  breakSkillTriggerConfigRepository(player)
                    .update(_.toggleBreakSkillTriggerConfig(configKey))
                )
              }

            override def breakSkillTriggerConfig(
              player: Player,
              configKey: BreakSkillTriggerConfigKey
            ): F[Boolean] =
              ContextCoercion(
                breakSkillTriggerConfigRepository(player)
                  .get
                  .map(_.breakSkillTriggerConfig(configKey))
              )
          }

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] = Seq(
          breakSkillTriggerConfigRepositoryControls.coerceFinalizationContextTo[F]
        )
      }
    }
  }

}
