package com.github.unchama.seichiassist.subsystems.chatratelimiter.bukkit.listeners

import cats.effect.{SyncEffect, SyncIO}
import com.github.unchama.seichiassist.subsystems.chatratelimiter.domain.{
  ChatPermissionRequestResult,
  ObtainChatPermission
}
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.{EventHandler, Listener}

class RateLimitCheckListener[F[_], G[_]: SyncEffect](
  implicit api: ObtainChatPermission[F, G, Player]
) extends Listener {
  @EventHandler
  def onEvent(e: AsyncPlayerChatEvent): Unit = {
    val player = e.getPlayer
    val requestResult = SyncEffect[G]
      .runSync[SyncIO, ChatPermissionRequestResult](api.forPlayer(player))
      .unsafeRunSync()

    if (requestResult == ChatPermissionRequestResult.Failed) {
      player.sendMessage(s"${ChatColor.RED}整地レベルが1であるため、あなたは30秒に1度しか発言できません。")
      e.setCancelled(true)
    }
  }
}
