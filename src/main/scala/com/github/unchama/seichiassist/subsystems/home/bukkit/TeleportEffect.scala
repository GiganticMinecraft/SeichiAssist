package com.github.unchama.seichiassist.subsystems.home.bukkit

import cats.data.Kleisli
import cats.effect.SyncIO
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import org.bukkit.Location
import org.bukkit.entity.Player

object TeleportEffect {
  def to[F[_]: OnMinecraftServerThread](location: Location): Kleisli[F, Player, Unit] = {
    Kleisli { player =>
      OnMinecraftServerThread[F].runAction(SyncIO {
        player.teleport(location)
        ()
      })
    }
  }
}
