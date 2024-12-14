package com.github.unchama.seichiassist.subsystems.breaksuppressionpreference

import cats.data.Kleisli
import cats.effect.SyncEffect
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breaksuppressionpreference.application.repository.BreakSuppressionPreferenceRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.breaksuppressionpreference.domain.{
  BreakSuppressionPreference,
  BreakSuppressionPreferencePersistence
}
import com.github.unchama.seichiassist.subsystems.breaksuppressionpreference.persistence.JdbcBreakSuppressionPreferencePersistence
import org.bukkit.entity.Player

trait System[F[_], Player] extends Subsystem[F] {
  val api: BreakSuppressionPreferenceAPI[F, Player]
}

object System {

  import cats.implicits._

  def wired[F[_], G[_]: SyncEffect: ContextCoercion[*[_], F]]: G[System[F, Player]] = {
    implicit val breakSuppressionPreferencePersistence
      : BreakSuppressionPreferencePersistence[G] =
      new JdbcBreakSuppressionPreferencePersistence[G]

    for {
      breakSuppressionPreferenceRepositoryControls <- BukkitRepositoryControls.createHandles(
        BreakSuppressionPreferenceRepositoryDefinition.withContext[G, Player]
      )
    } yield {
      val breakSuppressionPreferenceRepository =
        breakSuppressionPreferenceRepositoryControls.repository

      new System[F, Player] {
        override val api: BreakSuppressionPreferenceAPI[F, Player] =
          new BreakSuppressionPreferenceAPI[F, Player] {
            override def toggleBreakSuppression: Kleisli[F, Player, Unit] =
              Kleisli { player =>
                ContextCoercion(
                  breakSuppressionPreferenceRepository(player).update(pref =>
                    pref.copy(doBreakSuppression = !pref.doBreakSuppression)
                  )
                )
              }

            override def isBreakSuppressionEnabled(player: Player): F[Boolean] =
              ContextCoercion(
                breakSuppressionPreferenceRepository(player).get.map(_.doBreakSuppression)
              )
          }

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] = Seq(
          breakSuppressionPreferenceRepositoryControls.coerceFinalizationContextTo[F]
        )
      }
    }
  }

}
