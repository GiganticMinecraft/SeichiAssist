package com.github.unchama.seichiassist.subsystems.autosave

import cats.effect.IO
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.autosave.bukkit.task.global.WorldSaveRoutine

object System {
  def wired: Subsystem = {
    import PluginExecutionContexts._

    val repeatedJobs = List[IO[Nothing]](
      WorldSaveRoutine()
    )

    Subsystem(Seq(), Map(), repeatedJobs)
  }
}