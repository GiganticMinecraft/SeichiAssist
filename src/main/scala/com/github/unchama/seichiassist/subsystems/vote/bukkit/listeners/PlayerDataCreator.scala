package com.github.unchama.seichiassist.subsystems.vote.bukkit.listeners

import com.github.unchama.toIO

import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.ioRuntime

import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.vote.domain.VotePersistence
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

class PlayerDataCreator[F[_]: [f[_]] =>> ContextCoercion[f, cats.effect.IO]](
  implicit votePersistence: VotePersistence[F]
) extends Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  def onPlayerPreLoginEvent(e: AsyncPlayerPreLoginEvent): Unit = {
    votePersistence.createPlayerData(e.getUniqueId).toIO.unsafeRunAndForget()
  }

}
