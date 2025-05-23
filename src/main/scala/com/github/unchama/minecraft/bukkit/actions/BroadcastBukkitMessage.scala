package com.github.unchama.minecraft.bukkit.actions

import cats.effect.SyncIO
import com.github.unchama.minecraft.actions.{BroadcastMinecraftMessage, OnMinecraftServerThread}
import org.bukkit.Bukkit

class BroadcastBukkitMessage[F[_]: OnMinecraftServerThread]
    extends BroadcastMinecraftMessage[F] {

  import scala.jdk.CollectionConverters._

  override def string(message: String): F[Unit] = {
    OnMinecraftServerThread[F].runAction(SyncIO {
      // Bukkit.getOnlinePlayersは同期スレッドでアクセスしなければならない
      Bukkit.getOnlinePlayers.asScala.toList.foreach(_.sendMessage(message))
    })
  }
}

object BroadcastBukkitMessage {

  def apply[F[_]: OnMinecraftServerThread]: BroadcastMinecraftMessage[F] =
    new BroadcastBukkitMessage[F]

}
