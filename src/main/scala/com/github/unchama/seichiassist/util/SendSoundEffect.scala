package com.github.unchama.seichiassist.util

import cats.effect.IO
import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.{Bukkit, Sound}

object SendSoundEffect {

  import scala.jdk.CollectionConverters._

  def sendEverySound(kind: Sound, volume: Float, pitch: Float): Unit = {
    Bukkit
      .getOnlinePlayers
      .forEach(player => player.playSound(player.getLocation, kind, volume, pitch))
  }

  def sendEverySoundWithoutIgnore(kind: Sound, volume: Float, pitch: Float): Unit = {
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
            .getBroadcastMutingSettings
          _ <- IO {
            if (!settings.shouldMuteSounds)
              player.playSound(player.getLocation, kind, volume, pitch)
          }
        } yield ()
      }
      .unsafeRunSync()
  }
}
