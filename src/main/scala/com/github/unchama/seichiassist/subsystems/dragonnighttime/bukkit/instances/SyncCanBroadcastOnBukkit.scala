package com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit.instances

import cats.effect.Sync
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.dragonnighttime.application.CanBroadcast
import com.github.unchama.seichiassist.util.SendMessageEffect
import org.bukkit.Bukkit

object SyncCanBroadcastOnBukkit {
  def apply[F[_]: Sync]: CanBroadcast[F] = (message: String) =>
    Sync[F].delay {
      SendMessageEffect.sendMessageToEveryoneIgnoringPreference(message)
      Bukkit.getLogger.info(message)
    }
}
