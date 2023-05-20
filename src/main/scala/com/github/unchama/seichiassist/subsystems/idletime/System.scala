package com.github.unchama.seichiassist.subsystems.idletime

import cats.effect.{ContextShift, IO, LiftIO, Sync, SyncIO}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.datarepository.bukkit.player.{
  BukkitRepositoryControls,
  PlayerDataRepository
}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.idletime.application.repository.{
  IdleTimeRepositoryDefinitions,
  PlayerIdleTimeRecalculationRoutineFiberRepositoryDefinitions,
  PlayerLocationRepositoryDefinitions
}
import com.github.unchama.seichiassist.subsystems.idletime.bukkit.BukkitPlayerLocationRepository
import com.github.unchama.seichiassist.subsystems.idletime.bukkit.routines.BukkitPlayerIdleTimeRecalculationRoutine
import com.github.unchama.seichiassist.subsystems.idletime.domain.{
  PlayerIdleMinuteRepository,
  PlayerLocationRepository
}
import org.bukkit.Location
import org.bukkit.entity.Player

trait System[F[_], Player] extends Subsystem[F] {

  val api: IdleTimeAPI[F, Player]

}

object System {

  def wired[F[_]: Sync: LiftIO](
    implicit repeatingTaskContext: RepeatingTaskContext,
    onMainThread: OnMinecraftServerThread[IO],
    ioShift: ContextShift[IO]
  ): SyncIO[System[F, Player]] = {
    implicit val playerLocationRepository
      : Player => PlayerLocationRepository[SyncIO, Location, Player] =
      new BukkitPlayerLocationRepository[SyncIO](_)

    for {
      playerLocationRepositoryControls <- BukkitRepositoryControls.createHandles(
        RepositoryDefinition
          .Phased
          .TwoPhased(
            PlayerLocationRepositoryDefinitions.initialization[SyncIO, Location, Player],
            PlayerLocationRepositoryDefinitions.finalization[SyncIO, Location, Player]
          )
      )
      idleMinuteRepositoryControls <- BukkitRepositoryControls.createHandles(
        RepositoryDefinition
          .Phased
          .TwoPhased(
            IdleTimeRepositoryDefinitions.initialization[SyncIO, Player],
            IdleTimeRepositoryDefinitions.finalization[SyncIO, Player]
          )
      )
      playerAwayTimeRecalculationRoutineFiberRepositoryControls <- BukkitRepositoryControls
        .createHandles(
          RepositoryDefinition
            .Phased
            .TwoPhased(
              PlayerIdleTimeRecalculationRoutineFiberRepositoryDefinitions
                .initialization[SyncIO, Player] { player =>
                  implicit val idleTimeRepository
                    : PlayerDataRepository[PlayerIdleMinuteRepository[SyncIO]] =
                    idleMinuteRepositoryControls.repository
                  implicit val playerLocationRepository
                    : PlayerDataRepository[PlayerLocationRepository[SyncIO, Location, Player]] =
                    playerLocationRepositoryControls.repository

                  new BukkitPlayerIdleTimeRecalculationRoutine(player)
                },
              PlayerIdleTimeRecalculationRoutineFiberRepositoryDefinitions
                .finalization[SyncIO, Player]
            )
        )
    } yield {
      new System[F, Player] {
        override val api: IdleTimeAPI[F, Player] = (player: Player) =>
          idleMinuteRepositoryControls.repository(player).currentIdleMinute.to[F]

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] =
          Seq(
            playerLocationRepositoryControls,
            idleMinuteRepositoryControls,
            playerAwayTimeRecalculationRoutineFiberRepositoryControls
          ).map(_.coerceFinalizationContextTo[F])
      }
    }
  }

}
