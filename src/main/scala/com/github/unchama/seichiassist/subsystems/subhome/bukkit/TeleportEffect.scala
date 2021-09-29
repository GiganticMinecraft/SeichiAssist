package com.github.unchama.seichiassist.subsystems.subhome.bukkit

import cats.data.Kleisli
import cats.effect.Sync
import org.bukkit.Location
import org.bukkit.entity.Player

object TeleportEffect {
  def to[F[_]: Sync](location: Location): Kleisli[F, Player, Unit] = {
    Kleisli { player =>
      Sync[F].delay(player.teleport(location))
    }
  }
}
