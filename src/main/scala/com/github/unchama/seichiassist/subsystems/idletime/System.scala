package com.github.unchama.seichiassist.subsystems.idletime

import cats.effect.{Sync, SyncEffect}
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.idletime.application.repository.IdleMinuteRepositoryDefinitions
import org.bukkit.entity.Player

trait System[F[_], Player] extends Subsystem[F] {

  val api: IdleTimeAPI[F, Player]

}

object System {

  import cats.implicits._

  def wired[F[_]: Sync, G[_]: SyncEffect: ContextCoercion[*[_], F]]: G[System[F, Player]] = {
    for {
      idleMinuteRepositoryControls <- BukkitRepositoryControls.createHandles(
        RepositoryDefinition
          .Phased
          .TwoPhased(
            IdleMinuteRepositoryDefinitions.initialization[G, Player],
            IdleMinuteRepositoryDefinitions.finalization[G, Player]
          )
      )
    } yield {
      new System[F, Player] {
        override val api: IdleTimeAPI[F, Player] = (player: Player) =>
          ContextCoercion(idleMinuteRepositoryControls.repository(player).currentIdleMinute)

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] =
          Seq(idleMinuteRepositoryControls).map(_.coerceFinalizationContextTo[F])
      }
    }
  }

}
