package com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit.instances

import cats.effect.Sync
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.dragonnighttime.application.Notifiable
import com.github.unchama.seichiassist.util.SendMessageEffect
import org.bukkit.Bukkit

object SyncNotifiable {
  def apply[F[_]: Sync]: Notifiable[F] = (message: String) =>
    Sync[F].delay {
      SendMessageEffect.sendMessageToEveryoneIgnoringPreference(message)
      Bukkit.getLogger.info(message)
    }
}
