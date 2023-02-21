package com.github.unchama.seichiassist.subsystems.vote.bukkit.listeners

import cats.effect.ConcurrentEffect
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import com.github.unchama.seichiassist.subsystems.vote.domain.VotePersistence
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}

class PlayerDataCreator[F[_]: ConcurrentEffect](implicit votePersistence: VotePersistence[F])
    extends Listener {

  @EventHandler
  def onJoin(e: PlayerJoinEvent): Unit = {
    votePersistence.createPlayerData(e.getPlayer.getUniqueId).toIO.unsafeRunSync()
  }

}
