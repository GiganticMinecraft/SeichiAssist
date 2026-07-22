package com.github.unchama.seichiassist.subsystems.home.bukkit.listeners

import com.github.unchama.toIO

import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.ioRuntime

import cats.effect.Async
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.home.{HomeAPI, HomeReadAPI}
import com.github.unchama.seichiassist.subsystems.home.bukkit.LocationCodec
import com.github.unchama.seichiassist.subsystems.home.domain.{Home, HomeId}
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.{EventHandler, Listener}

class RespawnLocationOverwriter[
  F[_]: Async: HomeAPI: [f[_]] =>> ContextCoercion[f, cats.effect.IO]
] extends Listener {

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
