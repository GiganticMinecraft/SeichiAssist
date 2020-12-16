package com.github.unchama.seichiassist.subsystems.dragonnighttime

import cats.effect.{Sync, Timer}
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems.dragonnighttime.application.{CanAddEffect, CanNotifyStart, DragonNightTimeRoutine}
import com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit.instances.{SyncCanAddEffect, SyncCanNotifyStart}

object System {
  def wired[F[_] : Sync : Timer, G[_]]: StatefulSubsystem[F, List[F[Nothing]]] = {
    import PluginExecutionContexts._

    implicit val _canAddEffect: CanAddEffect[F] = SyncCanAddEffect[F]
    implicit val _canNotifyStart: CanNotifyStart[F] = SyncCanNotifyStart[F]

    val repeatedJobs = List[F[Nothing]](
      DragonNightTimeRoutine()
    )

    StatefulSubsystem[F, List[F[Nothing]]](Seq(), Nil, Map(), repeatedJobs)
  }
}
