package com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname

import cats.effect.{ContextShift, IO, Sync, SyncIO}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.idletime.IdleTimeAPI
import com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.application.repository.PlayerScreenNameUpdateRoutineFiberRepositoryDefinitions
import com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.bukkit.{
  BukkitNameColorByIdleMinute,
  BukkitPlayerScreenNameUpdateRoutine,
  BukkitUpdatePlayerScreenName
}
import com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.domain.{
  NameColorByIdleMinute,
  UpdatePlayerScreenName
}
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object System {

  def wired[F[_]: Sync](
    implicit repeatingTaskContext: RepeatingTaskContext,
    onMainThread: OnMinecraftServerThread[IO],
    ioShift: ContextShift[IO],
    idleTimeAPI: IdleTimeAPI[IO, Player]
  ): SyncIO[Subsystem[F]] = {
    implicit val nameColorByIdleMinute: NameColorByIdleMinute[ChatColor] =
      BukkitNameColorByIdleMinute
    implicit val updatePlayerScreenName: UpdatePlayerScreenName[IO, Player] =
      new BukkitUpdatePlayerScreenName[IO]

    for {
      playerScreenNameUpdateRoutineFiberRepositoryControls <- BukkitRepositoryControls
        .createHandles(
          RepositoryDefinition
            .Phased
            .TwoPhased(
              PlayerScreenNameUpdateRoutineFiberRepositoryDefinitions
                .initialization[SyncIO, Player](new BukkitPlayerScreenNameUpdateRoutine()),
              PlayerScreenNameUpdateRoutineFiberRepositoryDefinitions
                .finalization[SyncIO, Player]
            )
        )
    } yield {
      new Subsystem[F] {
        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] = Seq(
          playerScreenNameUpdateRoutineFiberRepositoryControls
        ).map(_.coerceFinalizationContextTo[F])
      }
    }
  }

}
