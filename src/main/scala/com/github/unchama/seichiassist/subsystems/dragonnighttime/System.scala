package com.github.unchama.seichiassist.subsystems.dragonnighttime

import cats.effect.IO
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit.task.global.DragonNightTimeRoutine

object System {
  def wired[F[_], G[_]: Sync: Timer]: StatefulSubsystem[F, List[G[Nothing]]] = {
    import PluginExecutionContexts._

    val repeatedJobs = List[IO[Nothing]](
      DragonNightTimeRoutine()
    )

    StatefulSubsystem[F, List[IO[Nothing]]](Seq(), Nil, Map(), repeatedJobs)
  }
}
