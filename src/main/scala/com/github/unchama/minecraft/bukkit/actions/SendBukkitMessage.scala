package com.github.unchama.minecraft.bukkit.actions

import cats.effect.Sync
import com.github.unchama.minecraft.actions.SendMinecraftMessage
import org.bukkit.entity.Player

class SendBukkitMessage[F[_]: Sync] extends SendMinecraftMessage[F, Player] {
  // NOTE: プレーヤーがオフラインの時にこのアクションを実行しても問題ない
  override def string(player: Player, s: String): F[Unit] =
    Sync[F].delay(player.sendMessage(s))
}

object SendBukkitMessage {

  implicit def apply[F[_]: Sync]: SendMinecraftMessage[F, Player] = new SendBukkitMessage[F]

}
