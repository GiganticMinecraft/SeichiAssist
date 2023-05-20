package com.github.unchama.seichiassist.subsystems.vote.bukkit.listeners

import cats.effect.ConcurrentEffect
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import com.github.unchama.seichiassist.subsystems.vote.domain.VotePersistence
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

class PlayerDataCreator[F[_]: ConcurrentEffect](implicit votePersistence: VotePersistence[F])
    extends Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  def onPlayerPreLoginEvent(e: AsyncPlayerPreLoginEvent): Unit = {
    votePersistence.createPlayerData(e.getUniqueId).toIO.unsafeRunAsyncAndForget()
  }

}
