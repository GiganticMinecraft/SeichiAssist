package com.github.unchama.seichiassist.subsystems.home.bukkit

import cats.data.Kleisli
import cats.effect.{Sync, SyncIO}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object TeleportEffect {
  def to[F[_]: OnMinecraftServerThread: Sync](
    location: Location
  ): Kleisli[F, CommandSender, Unit] = {
    TargetedEffect.delay {
      case player: Player =>
        OnMinecraftServerThread[F].runAction(SyncIO {
          player.teleport(location)
        })
      case _ =>
    }
  }
}
