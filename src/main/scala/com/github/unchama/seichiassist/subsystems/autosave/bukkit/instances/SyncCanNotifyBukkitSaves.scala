package com.github.unchama.seichiassist.subsystems.autosave.bukkit.instances

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.autosave.application.CanNotifySaves
import com.github.unchama.seichiassist.util.Util
import org.bukkit.Bukkit

trait SyncCanNotifyBukkitSaves[F[_]] extends CanNotifySaves[F] {

  val F: Sync[F]

  final override def notify(message: String): F[Unit] = F.delay {
    Util.sendEveryMessage(message)
    Bukkit.getLogger.info(message)
  }

}
