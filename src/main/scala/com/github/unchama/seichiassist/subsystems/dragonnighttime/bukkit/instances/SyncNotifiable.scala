package com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit.instances

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.dragonnighttime.application.Notifiable
import com.github.unchama.seichiassist.util.Util
import org.bukkit.Bukkit

object SyncNotifiable {
  def apply[F[_] : Sync]: Notifiable[F] = (message: String) => Sync[F].delay {
    Util.sendEveryMessage(message)
    Bukkit.getLogger.info(message)
  }
}
