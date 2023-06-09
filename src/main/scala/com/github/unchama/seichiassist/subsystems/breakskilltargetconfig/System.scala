package com.github.unchama.seichiassist.subsystems.breakskilltargetconfig

import cats.data.Kleisli
import cats.effect.concurrent.Ref
import cats.effect.{Sync, SyncEffect}
import com.github.unchama.datarepository.bukkit.player.{
  BukkitRepositoryControls,
  PlayerDataRepository
}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.application.repository.BreakSkillTargetConfigRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.bukkit.BukkitBreakSkillTargetConfigRepository
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain.{
  BreakSkillTargetConfig,
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
    implicit val breakFlagPersistence: BreakSkillTargetConfigPersistence[G] =
      new JdbcBreakSkillTargetConfigPersistence[G]

    for {
      breakFlagRepositoryControls <- BukkitRepositoryControls.createHandles(
        BreakSkillTargetConfigRepositoryDefinition.withContext[G, Player]
      )
    } yield {
      implicit val breakFlagRepository
        : PlayerDataRepository[Ref[G, Set[BreakSkillTargetConfig]]] =
        breakFlagRepositoryControls.repository
      val breakSkillTargetConfigRepository = new BukkitBreakSkillTargetConfigRepository[G]

      new System[F, Player] {
        override val api: BreakSkillTargetConfigAPI[F, Player] =
          new BreakSkillTargetConfigAPI[F, Player] {
            override def toggleBreakSkillTargetConfig(
              breakFlagName: BreakSkillTargetConfigKey
            ): Kleisli[F, Player, Unit] =
              Kleisli { player =>
                ContextCoercion(
                  breakSkillTargetConfigRepository
                    .toggleBreakSkillTargetConfig(player, breakFlagName)
                )
              }

            override def breakSkillTargetConfig(
              player: Player,
              breakFlagName: BreakSkillTargetConfigKey
            ): F[Boolean] =
              ContextCoercion(
                breakSkillTargetConfigRepository.breakSkillTargetConfig(player, breakFlagName)
              )
          }

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] = Seq(
          breakFlagRepositoryControls.coerceFinalizationContextTo[F]
        )
      }
    }
  }

}
