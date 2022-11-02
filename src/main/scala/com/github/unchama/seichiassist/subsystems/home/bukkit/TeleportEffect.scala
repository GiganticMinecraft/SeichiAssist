package com.github.unchama.seichiassist.subsystems.home.bukkit

import cats.data.Kleisli
import cats.effect.SyncIO
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object TeleportEffect {
  def to[F[_]: OnMinecraftServerThread](location: Location): Kleisli[F, CommandSender, Unit] = {
    Kleisli { player =>
      OnMinecraftServerThread[F].runAction(SyncIO {
        player match {
          case p: Player => p.teleport(location)
          case _         => throw new RuntimeException("This branch should not be reached.")
        }
        ()
      })
    }
  }
}
