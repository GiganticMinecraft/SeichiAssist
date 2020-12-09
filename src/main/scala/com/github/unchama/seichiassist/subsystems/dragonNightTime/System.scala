package com.github.unchama.seichiassist.subsystems.dragonnighttime

import cats.effect.IO
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit.task.global.DragonNightTimeRoutine

object System {
  def wired: StatefulSubsystem[List[IO[Nothing]]] = {
    import PluginExecutionContexts._

    val repeatedJobs = List[IO[Nothing]](
      DragonNightTimeRoutine()
    )

    StatefulSubsystem[List[IO[Nothing]]](Seq(), Map(), repeatedJobs)
  }
}
