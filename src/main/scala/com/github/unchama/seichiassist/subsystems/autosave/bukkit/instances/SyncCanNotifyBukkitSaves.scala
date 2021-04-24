package com.github.unchama.seichiassist.subsystems.autosave.bukkit.instances

import cats.effect.Sync
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.autosave.application.CanNotifySaves
import com.github.unchama.seichiassist.util.Util
import org.bukkit.Bukkit

object SyncCanNotifyBukkitSaves {

  def apply[F[_] : Sync]: CanNotifySaves[F] = (message: String) => Sync[F].delay {
    Util.sendEveryMessageIgnoringPreference(message)
    Bukkit.getLogger.info(message)
  }

}
