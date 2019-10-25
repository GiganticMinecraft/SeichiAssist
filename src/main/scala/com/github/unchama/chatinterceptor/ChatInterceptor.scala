package com.github.unchama.chatinterceptor

import java.util.UUID

import org.bukkit.event.player.{AsyncPlayerChatEvent, PlayerQuitEvent}
import org.bukkit.event.{EventHandler, Listener}

class ChatInterceptor(val scopes: List[InterceptionScope[UUID, String]]) extends Listener {
  @EventHandler
  def onPlayerQuit(event: PlayerQuitEvent): Unit = {
    import cats.implicits._

    scopes
      .map(_.cancelAnyInterception(event.getPlayer.getUniqueId, CancellationReason.PlayerQuit))
      .sequence
      .unsafeRunSync()
  }

  @EventHandler
  def onPlayerChat(event: AsyncPlayerChatEvent): Unit = {
    import cats.implicits._

    scopes
      .map(_.suggestInterception(event.getPlayer.getUniqueId, event.getMessage))
      .sequence
      .unsafeRunSync()
      .find(_ == InterceptorResponse.Intercepted) match {
      case Some(_) =>
        event.setCancelled(true)
    }
  }
}
