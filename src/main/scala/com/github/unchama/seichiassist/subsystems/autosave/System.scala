package com.github.unchama.seichiassist.subsystems.autosave

import cats.effect.{Sync, Timer}
import com.github.unchama.concurrent.MinecraftServerThreadShift
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems.autosave.application.{CanNotifySaves, CanSaveWorlds, SystemConfiguration, WorldSaveRoutine}
import com.github.unchama.seichiassist.subsystems.autosave.bukkit.instances.{SyncCanNotifyBukkitSaves, SyncCanSaveBukkitWorlds}

object System {
  def wired[
    F[_] : Sync : Timer : MinecraftServerThreadShift,
    G[_]
  ](configuration: SystemConfiguration): StatefulSubsystem[G, List[F[Nothing]]] = {
    import PluginExecutionContexts._

    implicit val _configuration: SystemConfiguration = configuration

    implicit val _canSaveWorlds: CanSaveWorlds[F] = SyncCanSaveBukkitWorlds[F]
    implicit val _canNotifySaves: CanNotifySaves[F] = SyncCanNotifyBukkitSaves[F]

    val repeatedJobs = List[F[Nothing]](
      WorldSaveRoutine()
    )

    StatefulSubsystem[G, List[F[Nothing]]](Seq(), Nil, Map(), repeatedJobs)
  }
}
