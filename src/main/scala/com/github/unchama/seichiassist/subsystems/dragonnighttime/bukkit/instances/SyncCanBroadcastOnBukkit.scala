package com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit.instances

import cats.effect.Sync
import com.github.unchama.minecraft.actions.{GetConnectedPlayers, OnMinecraftServerThread}
import com.github.unchama.seichiassist.subsystems.dragonnighttime.application.CanBroadcast
import com.github.unchama.seichiassist.util.SendMessageEffect
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object SyncCanBroadcastOnBukkit {
  import cats.implicits._

  def apply[F[_]: Sync: OnMinecraftServerThread: [f[_]] =>> GetConnectedPlayers[f, Player]]
    : CanBroadcast[F] = (message: String) => {
    for {
      _ <- SendMessageEffect.sendMessageToEveryoneIgnoringPreferenceM[String, F](message)
      _ <- Sync[F].delay(Bukkit.getLogger.info(message))
    } yield ()
  }
}
