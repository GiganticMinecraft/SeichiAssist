package com.github.unchama.minecraft.actions

import org.bukkit.Sound

trait BroadCastMinecraftSound[F[_]] {

  def playSound(sound: Sound, volume: Float, pitch: Float): F[Unit]

}

object BroadCastMinecraftSound {

  def apply[F[_]](implicit ev: BroadCastMinecraftSound[F]): BroadCastMinecraftSound[F] =
    ev

}
