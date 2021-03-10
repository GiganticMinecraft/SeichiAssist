package com.github.unchama.seichiassist.subsystems.autosave

import cats.effect.{Sync, Timer}
import com.github.unchama.minecraft.actions.MinecraftServerThreadShift
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.autosave.application.{CanNotifySaves, CanSaveWorlds, SystemConfiguration, WorldSaveRoutine}
import com.github.unchama.seichiassist.subsystems.autosave.bukkit.instances.{SyncCanNotifyBukkitSaves, SyncCanSaveBukkitWorlds}

trait System[F[_]] extends Subsystem[F] {
  val routine: F[Nothing]
}

object System {
  def backgroundProcess[
    F[_] : Sync : Timer : MinecraftServerThreadShift,
    G[_]
  ](configuration: SystemConfiguration): F[Nothing] = {
    import PluginExecutionContexts._

    implicit val _configuration: SystemConfiguration = configuration

    implicit val _canSaveWorlds: CanSaveWorlds[F] = SyncCanSaveBukkitWorlds[F]
    implicit val _canNotifySaves: CanNotifySaves[F] = SyncCanNotifyBukkitSaves[F]

    WorldSaveRoutine()
  }
}
