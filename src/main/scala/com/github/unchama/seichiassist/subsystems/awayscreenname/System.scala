package com.github.unchama.seichiassist.subsystems.awayscreenname

import cats.effect.{ContextShift, IO, Sync, SyncIO}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.awayscreenname.application.repository.{
  IdleMinuteRepositoryDefinitions,
  PlayerAwayTimeRecalculationRoutineFiberRepositoryDefinitions,
  PlayerLocationRepositoryDefinitions
}
import com.github.unchama.seichiassist.subsystems.awayscreenname.bukkit.BukkitPlayerLocationRepository
import com.github.unchama.seichiassist.subsystems.awayscreenname.bukkit.routines.BukkitPlayerAwayTimeRecalculationRoutine
import com.github.unchama.seichiassist.subsystems.awayscreenname.domain.{
  IdleMinute,
  PlayerLocationRepository
}
import org.bukkit.Location
import org.bukkit.entity.Player

import java.util.UUID

trait System[F[_]] extends Subsystem[F] {

  val api: AwayScreenNameAPI[F]

}

object System {

  def wired[F[_]: Sync](
    implicit repeatingTaskContext: RepeatingTaskContext,
    onMainThread: OnMinecraftServerThread[IO],
    ioShift: ContextShift[IO]
  ): SyncIO[System[F]] = {
    implicit val playerLocationRepository
      : Player => PlayerLocationRepository[SyncIO, Location, Player] =
      new BukkitPlayerLocationRepository[SyncIO](_)

    for {
      idleMinuteRepositoryControls <- BukkitRepositoryControls.createHandles(
        RepositoryDefinition
          .Phased
          .TwoPhased(
            IdleMinuteRepositoryDefinitions.initialization[SyncIO, Player],
            IdleMinuteRepositoryDefinitions.finalization[SyncIO, Player]
          )
      )
      playerLocationRepositoryControls <- BukkitRepositoryControls.createHandles(
        RepositoryDefinition
          .Phased
          .TwoPhased(
            PlayerLocationRepositoryDefinitions.initialization[SyncIO, Location, Player],
            PlayerLocationRepositoryDefinitions.finalization[SyncIO, Location, Player]
          )
      )
      playerAwayTimeRecalculationRoutineFiberRepositoryControls <- BukkitRepositoryControls
        .createHandles(
          RepositoryDefinition
            .Phased
            .TwoPhased(
              PlayerAwayTimeRecalculationRoutineFiberRepositoryDefinitions
                .initialization[SyncIO, Player](player =>
                  new BukkitPlayerAwayTimeRecalculationRoutine(player)(
                    playerLocationRepositoryControls.repository,
                    idleMinuteRepositoryControls.repository
                  )
                ),
              PlayerAwayTimeRecalculationRoutineFiberRepositoryDefinitions
                .finalization[SyncIO, Player]
            )
        )
    } yield {
      new System[F] {
        override val api: AwayScreenNameAPI[F] = new AwayScreenNameAPI[F] {

          /**
           * @return 指定したUUIDプレイヤーの[[IdleMinute]]を返す作用
           */
          override def idleTime(uuid: UUID): F[IdleMinute] = ???
        }

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] =
          Seq(
            idleMinuteRepositoryControls,
            playerLocationRepositoryControls,
            playerAwayTimeRecalculationRoutineFiberRepositoryControls
          ).map(_.coerceFinalizationContextTo[F])
      }
    }
  }

}
