package com.github.unchama.seichiassist.subsystems.autosave.bukkit.instances

import cats.effect.Sync
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.autosave.application.CanNotifySaves
import com.github.unchama.seichiassist.util.SendMessageEffect
import org.bukkit.Bukkit

object SyncCanNotifyBukkitSaves {

  def apply[F[_]: Sync]: CanNotifySaves[F] = (message: String) =>
    Sync[F].delay {
      SendMessageEffect.sendMessageToEveryoneIgnoringPreference(message)
      Bukkit.getLogger.info(message)
    }

}
