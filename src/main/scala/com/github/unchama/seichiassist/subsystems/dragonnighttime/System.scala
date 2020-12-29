package com.github.unchama.seichiassist.subsystems.dragonnighttime

import cats.effect.{Sync, Timer}
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems.dragonnighttime.application._
import com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit.instances._

object System {
  def wired[F[_] : Sync : Timer, G[_]]: StatefulSubsystem[F, List[F[Nothing]]] = {
    import PluginExecutionContexts._

    implicit val _addableWithContext: AddableWithContext[F] = SyncAddableWithContext[F]
    implicit val _notifiable: Notifiable[F] = SyncNotifiable[F]

    val repeatedJobs = List[F[Nothing]](
      DragonNightTimeRoutine()
    )

    StatefulSubsystem[F, List[F[Nothing]]](Seq(), Nil, Map(), repeatedJobs)
  }
}
