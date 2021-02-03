package com.github.unchama.minecraft.actions

trait BroadcastMinecraftMessage[F[_]] {

  def string(s: String): F[Unit]

}

object BroadcastMinecraftMessage {

  def apply[F[_]](implicit ev: BroadcastMinecraftMessage[F]): BroadcastMinecraftMessage[F] = ev

}
