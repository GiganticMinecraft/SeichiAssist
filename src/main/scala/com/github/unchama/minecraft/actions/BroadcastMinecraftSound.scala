package com.github.unchama.minecraft.actions

import org.bukkit.Sound

trait BroadcastMinecraftSound[F[_]] {

  def playSound(sound: Sound, volume: Float, pitch: Float): F[Unit]

}

object BroadcastMinecraftSound {

  def apply[F[_]](implicit ev: BroadcastMinecraftSound[F]): BroadcastMinecraftSound[F] =
    ev

}
