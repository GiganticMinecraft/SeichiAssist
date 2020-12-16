package com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit.instances

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.dragonnighttime.application.CanNotifyStart
import com.github.unchama.seichiassist.util.Util
import org.bukkit.Bukkit

object SyncCanNotifyStart {
  def apply[F[_] : Sync]: CanNotifyStart[F] = (message: String) => Sync[F].delay {
    Util.sendEveryMessage(message)
    Bukkit.getLogger.info(message)
  }
}
