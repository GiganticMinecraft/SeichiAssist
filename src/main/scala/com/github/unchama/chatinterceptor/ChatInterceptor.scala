package com.github.unchama.chatinterceptor

import org.bukkit.event.player.{AsyncPlayerChatEvent, PlayerQuitEvent}
import org.bukkit.event.{EventHandler, EventPriority, Listener}

class ChatInterceptor(val scopes: List[ChatInterceptionScope]) extends Listener {
  @EventHandler
  def onPlayerQuit(event: PlayerQuitEvent): Unit = {
    import cats.implicits._

    scopes
      .traverse(
        _.cancelAnyInterception(event.getPlayer.getUniqueId, CancellationReason.PlayerQuit)
      )
      .unsafeRunSync()
  }

  @EventHandler(priority = EventPriority.LOWEST)
  def onPlayerChat(event: AsyncPlayerChatEvent): Unit = {
    import cats.implicits._

    scopes
      .traverse(_.suggestInterception(event.getPlayer.getUniqueId, event.getMessage))
      .unsafeRunSync()
      .find(_ == InterceptorResponse.Intercepted) match {
      case Some(_) => event.setCancelled(true)
      case None    =>
    }
  }
}
