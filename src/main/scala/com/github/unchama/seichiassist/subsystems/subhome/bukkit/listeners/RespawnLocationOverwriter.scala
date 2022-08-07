package com.github.unchama.seichiassist.subsystems.subhome.bukkit.listeners

import cats.effect.ConcurrentEffect
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import com.github.unchama.seichiassist.subsystems.subhome.bukkit.LocationCodec
import com.github.unchama.seichiassist.subsystems.subhome.domain.{SubHome, SubHomeId}
import com.github.unchama.seichiassist.subsystems.subhome.{SubHomeAPI, SubHomeReadAPI}
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.{EventHandler, Listener}

class RespawnLocationOverwriter[F[_]: ConcurrentEffect: SubHomeAPI] extends Listener {

  @EventHandler
  def onRespawn(event: PlayerRespawnEvent): Unit = {
    val player = event.getPlayer
    for {
      SubHome(_, subHomeLocation) <- SubHomeReadAPI[F]
        .get(player.getUniqueId, SubHomeId(1))
        .toIO
        .unsafeRunSync()
      bukkitLocation <- LocationCodec.toBukkitLocation(subHomeLocation)
    } yield player.teleport(bukkitLocation)
  }

}
