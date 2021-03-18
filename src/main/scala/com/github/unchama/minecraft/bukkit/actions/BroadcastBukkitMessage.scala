package com.github.unchama.minecraft.bukkit.actions

import cats.effect.Sync
import com.github.unchama.minecraft.actions.{BroadcastMinecraftMessage, MinecraftServerThreadShift}
import org.bukkit.Bukkit

class BroadcastBukkitMessage[
  F[_] : Sync
](implicit serverThreadShift: MinecraftServerThreadShift[F]) extends BroadcastMinecraftMessage[F] {

  import cats.implicits._

  import scala.jdk.CollectionConverters._

  override def string(message: String): F[Unit] =
    serverThreadShift.shift >>
      Sync[F].delay {
        // Bukkit.getOnlinePlayersは同期スレッドでアクセスしなければならない
        Bukkit.getOnlinePlayers.asScala.toList.foreach(_.sendMessage(message))
      }
}

object BroadcastBukkitMessage {

  def apply[F[_] : Sync : MinecraftServerThreadShift]: BroadcastMinecraftMessage[F] =
    new BroadcastBukkitMessage[F]

}
