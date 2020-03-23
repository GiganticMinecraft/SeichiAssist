package com.github.unchama.seichiassist.effects.unfocused

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object BroadcastEffect {
  def apply(effect: TargetedEffect[Player]): TargetedEffect[Any] =
    Kleisli { _ =>
      import cats.implicits._

      import scala.jdk.CollectionConverters._

      for {
        players <- IO(synchronized(Bukkit.getOnlinePlayers.asScala.toList))
        _ <- players.map(effect.run).sequence
      } yield ()
    }
}
