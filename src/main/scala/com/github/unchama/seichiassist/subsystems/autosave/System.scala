package com.github.unchama.seichiassist.subsystems.autosave

import cats.effect.{Sync, Timer}
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems.autosave.application.{CanNotifySaves, CanSaveWorlds, SystemConfiguration}
import com.github.unchama.seichiassist.subsystems.autosave.bukkit.instances.{SyncCanNotifyBukkitSaves, SyncCanSaveBukkitWorlds}
import com.github.unchama.seichiassist.subsystems.autosave.bukkit.task.WorldSaveRoutine

object System {
  def wired[
    F[_] : Sync : Timer,
    G[_]
  ](configuration: SystemConfiguration): StatefulSubsystem[G, List[F[Nothing]]] = {
    import PluginExecutionContexts._

    implicit val _configuration: SystemConfiguration = configuration
    implicit val _instances: CanNotifySaves[F] with CanSaveWorlds[F] =
      new SyncCanNotifyBukkitSaves[F] with SyncCanSaveBukkitWorlds[F] {
        override val F: Sync[F] = implicitly
      }

    val repeatedJobs = List[F[Nothing]](
      WorldSaveRoutine()
    )

    StatefulSubsystem[G, List[F[Nothing]]](Seq(), Nil, Map(), repeatedJobs)
  }
}
