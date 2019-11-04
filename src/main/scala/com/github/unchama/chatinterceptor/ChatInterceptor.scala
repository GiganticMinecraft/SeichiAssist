package com.github.unchama.chatinterceptor

import java.util.UUID

import com.github.unchama.chatinterceptor.ChatInterceptor.ChatInterceptionScope
import org.bukkit.event.player.{AsyncPlayerChatEvent, PlayerQuitEvent}
import org.bukkit.event.{EventHandler, EventPriority, Listener}

class ChatInterceptor(val scopes: List[ChatInterceptionScope]) extends Listener {
  @EventHandler
  def onPlayerQuit(event: PlayerQuitEvent): Unit = {
    import cats.implicits._

    scopes
      .map(_.cancelAnyInterception(event.getPlayer.getUniqueId, CancellationReason.PlayerQuit))
      .sequence
      .unsafeRunSync()
  }

  @EventHandler(priority=EventPriority.LOWEST)
  def onPlayerChat(event: AsyncPlayerChatEvent): Unit = {
    import cats.implicits._

    scopes
      .map(_.suggestInterception(event.getPlayer.getUniqueId, event.getMessage))
      .sequence
      .unsafeRunSync()
      .find(_ == InterceptorResponse.Intercepted) match {
      case Some(_) => event.setCancelled(true)
      case None =>
    }
  }
}

object ChatInterceptor {
  type ChatInterceptionScope = InterceptionScope[UUID, String]
}
