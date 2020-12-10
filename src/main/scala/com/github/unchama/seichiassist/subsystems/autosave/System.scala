package com.github.unchama.seichiassist.subsystems.autosave

import cats.effect.IO
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems.autosave.application.SystemConfiguration
import com.github.unchama.seichiassist.subsystems.autosave.bukkit.task.global.WorldSaveRoutine

object System {
  def wired[F[_]](configuration: SystemConfiguration): StatefulSubsystem[F, List[IO[Nothing]]] = {
    import PluginExecutionContexts._

    implicit val _configuration: SystemConfiguration = configuration

    val repeatedJobs = List[IO[Nothing]](
      WorldSaveRoutine()
    )

    StatefulSubsystem[F, List[IO[Nothing]]](Seq(), Nil, Map(), repeatedJobs)
  }
}
