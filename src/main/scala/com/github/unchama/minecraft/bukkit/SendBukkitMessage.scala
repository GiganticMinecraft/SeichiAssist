package com.github.unchama.minecraft.bukkit

import cats.effect.Sync
import com.github.unchama.minecraft.actions.SendMinecraftMessage
import org.bukkit.entity.Player

class SendBukkitMessage[F[_] : Sync] extends SendMinecraftMessage[F, Player] {
  override def string(player: Player, s: String): F[Unit] =
    Sync[F].delay(player.sendMessage(s))
}
