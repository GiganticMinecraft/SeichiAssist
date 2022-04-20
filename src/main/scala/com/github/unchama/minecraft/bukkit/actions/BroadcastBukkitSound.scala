package com.github.unchama.minecraft.bukkit.actions

import cats.effect.Sync
import com.github.unchama.minecraft.actions.{BroadcastMinecraftSound, OnMinecraftServerThread}
import com.github.unchama.seichiassist.effects.unfocused.BroadcastSoundEffect
import org.bukkit.Sound

class BroadcastBukkitSound[F[_]: Sync] extends BroadcastMinecraftSound[F] {

  override def playSound(sound: Sound, volume: Float, pitch: Float): F[Unit] = {
    // FIXME: これでは何も実行されない。
    // 本来 `F` には `OnMinecraftServerThread` が必要で、
    // この関数は BroadcastBukkitMessage と同じように実装すべき。
    // ref. https://github.com/GiganticMinecraft/SeichiAssist/blob/fdd279a33d11cc5d0d7f3e03ef3790f4117beef7/src/main/scala/com/github/unchama/minecraft/bukkit/actions/BroadcastBukkitMessage.scala#L13-L16
    // 現在、これを利用しているbuildcountシステムの設計が微妙(incrementとnotifyが同時に行われてしまっている)なので、
    // そこを改善してからでないとこの書き換えができない。
    Sync[F].delay {
      BroadcastSoundEffect(sound, volume, pitch)
    }
  }

}

object BroadcastBukkitSound {

  def apply[F[_]: Sync]: BroadcastMinecraftSound[F] =
    new BroadcastBukkitSound[F]

}
