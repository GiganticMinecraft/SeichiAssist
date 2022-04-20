package com.github.unchama.minecraft.bukkit.actions

import cats.effect.Sync
import com.github.unchama.minecraft.actions.{BroadCastMinecraftSound, OnMinecraftServerThread}
import com.github.unchama.seichiassist.effects.unfocused.BroadcastSoundEffect
import org.bukkit.Sound

class BroadcastBukkitSound[F[_]: Sync] extends BroadCastMinecraftSound[F] {

  override def playSound(sound: Sound, volume: Float, pitch: Float): F[Unit] = {
    Sync[F].delay {
      BroadcastSoundEffect(sound, volume, pitch)
    }
  }

}

object BroadcastBukkitSound {

  def apply[F[_]: Sync]: BroadCastMinecraftSound[F] =
    new BroadcastBukkitSound[F]

}
