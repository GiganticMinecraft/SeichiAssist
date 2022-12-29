package com.github.unchama.seichiassist.subsystems.lastquit.bukkit.listeners

import cats.effect.ConcurrentEffect
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import com.github.unchama.seichiassist.subsystems.lastquit.LastQuitAPI
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.{EventHandler, Listener}

class LastQuitUpdater[F[_]: ConcurrentEffect](implicit lastQuitAPI: LastQuitAPI[F])
    extends Listener {

  @EventHandler
  def onQuit(event: PlayerQuitEvent): Unit = {
    val uuid = event.getPlayer.getUniqueId
    lastQuitAPI.updateLastLastQuitDateTimeNow(uuid).toIO.unsafeRunAsyncAndForget()
  }

}
