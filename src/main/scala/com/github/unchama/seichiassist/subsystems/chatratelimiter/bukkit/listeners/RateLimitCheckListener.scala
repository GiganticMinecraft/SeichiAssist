package com.github.unchama.seichiassist.subsystems.chatratelimiter.bukkit.listeners

import cats.effect.{SyncEffect, SyncIO}
import com.github.unchama.seichiassist.subsystems.chatratelimiter.InspectChatRateLimit
import com.github.unchama.seichiassist.subsystems.chatratelimiter.domain.ChatPermissionRequestResult
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.{EventHandler, Listener}

class RateLimitCheckListener[F[_] : SyncEffect](implicit api: InspectChatRateLimit[F, Player]) extends Listener {
  @EventHandler
  def onEvent(e: AsyncPlayerChatEvent): Unit = {
    val player = e.getPlayer
    val requestResult = SyncEffect[F].runSync[SyncIO, ChatPermissionRequestResult](api.tryPermitted(player)).unsafeRunSync()
    if (requestResult == ChatPermissionRequestResult.Failed) {
      e.setCancelled(true)
    }
  }
}
