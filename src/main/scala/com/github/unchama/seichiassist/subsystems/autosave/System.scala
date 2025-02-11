package com.github.unchama.seichiassist.subsystems.autosave

import cats.effect.{Sync, Timer}
import com.github.unchama.minecraft.actions.{GetConnectedPlayers, OnMinecraftServerThread}
import com.github.unchama.seichiassist.subsystems.autosave.application.{
  CanNotifySaves,
  CanSaveWorlds,
  SystemConfiguration,
  WorldSaveRoutine
}
import com.github.unchama.seichiassist.subsystems.autosave.bukkit.instances.{
  SyncCanNotifyBukkitSaves,
  SyncCanSaveBukkitWorlds
}
import org.bukkit.entity.Player

object System {
  def backgroundProcess[F[_]: Sync: Timer: OnMinecraftServerThread](
    implicit getConnectedPlayers: GetConnectedPlayers[F, Player],
    configuration: SystemConfiguration
  ): F[Nothing] = {

    implicit val _canSaveWorlds: CanSaveWorlds[F] = SyncCanSaveBukkitWorlds[F]
    implicit val _canNotifySaves: CanNotifySaves[F] =
      SyncCanNotifyBukkitSaves[F]

    WorldSaveRoutine()
  }
}
