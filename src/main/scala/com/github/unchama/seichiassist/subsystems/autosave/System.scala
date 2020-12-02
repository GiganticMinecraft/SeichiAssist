package com.github.unchama.seichiassist.subsystems.autosave

import cats.effect.IO
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems.autosave.bukkit.task.global.WorldSaveRoutine

object System {
  def wired: StatefulSubsystem[List[IO[Nothing]]] = {
    import PluginExecutionContexts._

    val repeatedJobs = List[IO[Nothing]](
      WorldSaveRoutine()
    )

    StatefulSubsystem[List[IO[Nothing]]](Seq(), Map(), repeatedJobs)
  }
}
