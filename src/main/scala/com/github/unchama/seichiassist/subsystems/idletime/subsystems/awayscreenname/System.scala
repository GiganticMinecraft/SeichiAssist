package com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname

import cats.effect.{ContextShift, IO, LiftIO, Sync, SyncIO}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.idletime.IdleTimeAPI
import org.bukkit.entity.Player

object System {

  def wired[F[_]: Sync: LiftIO](
    implicit repeatingTaskContext: RepeatingTaskContext,
    onMainThread: OnMinecraftServerThread[IO],
    ioShift: ContextShift[IO],
    idleTimeAPI: IdleTimeAPI[IO, Player]
  ): SyncIO[Subsystem[F]] = {}

}
