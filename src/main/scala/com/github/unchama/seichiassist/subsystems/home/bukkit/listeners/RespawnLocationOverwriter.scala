package com.github.unchama.seichiassist.subsystems.home.bukkit.listeners

import cats.effect.ConcurrentEffect
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import com.github.unchama.seichiassist.subsystems.home.{HomeAPI, HomeReadAPI}
import com.github.unchama.seichiassist.subsystems.home.bukkit.LocationCodec
import com.github.unchama.seichiassist.subsystems.home.domain.{Home, HomeId}
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.{EventHandler, Listener}

class RespawnLocationOverwriter[F[_]: ConcurrentEffect: HomeAPI] extends Listener {

  @EventHandler
  def onRespawn(event: PlayerRespawnEvent): Unit = {
    val player = event.getPlayer
    for {
      Home(_, homeLocation) <- HomeReadAPI[F]
        .get(player.getUniqueId, HomeId(1))
        .toIO
        .unsafeRunSync()
      bukkitLocation <- LocationCodec.toBukkitLocation(homeLocation)
    } yield player.teleport(bukkitLocation)
  }

}
