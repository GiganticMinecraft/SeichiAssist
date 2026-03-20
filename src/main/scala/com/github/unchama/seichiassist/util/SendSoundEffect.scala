package com.github.unchama.seichiassist.util

import cats.effect.Sync
import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.{Bukkit, Sound}

object SendSoundEffect {

  import scala.jdk.CollectionConverters._

  def sendEverySound[F[_]: Sync](kind: Sound, volume: Float, pitch: Float): F[Unit] =
    Sync[F].delay {
      Bukkit
        .getOnlinePlayers
        .forEach(player => player.playSound(player.getLocation, kind, volume, pitch))
    }

  def sendEverySoundWithoutIgnore[F[_]: Sync](
    kind: Sound,
    volume: Float,
    pitch: Float
  ): F[Unit] = {
    import cats.implicits._

    Bukkit
      .getOnlinePlayers
      .asScala
      .toList
      .traverse { player =>
        for {
          settings <- SeichiAssist
            .playermap(player.getUniqueId)
            .settings
            .getBroadcastMutingSettings[F]
          _ <- Sync[F].delay {
            if (!settings.shouldMuteSounds)
              player.playSound(player.getLocation, kind, volume, pitch)
          }
        } yield ()
      }
      .void
  }
}
