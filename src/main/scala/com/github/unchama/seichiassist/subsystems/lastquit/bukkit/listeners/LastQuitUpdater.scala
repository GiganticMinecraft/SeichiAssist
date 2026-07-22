package com.github.unchama.seichiassist.subsystems.lastquit.bukkit.listeners

import com.github.unchama.toIO

import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.ioRuntime

import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.lastquit.LastQuitAPI
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.{EventHandler, Listener}

class LastQuitUpdater[F[_]: [f[_]] =>> ContextCoercion[f, cats.effect.IO]](
  implicit lastQuitAPI: LastQuitAPI[F]
) extends Listener {

  @EventHandler
  def onQuit(event: PlayerQuitEvent): Unit = {
    val uuid = event.getPlayer.getUniqueId
    lastQuitAPI.updateLastLastQuitDateTimeNow(uuid).toIO.unsafeRunAndForget()
  }

}
