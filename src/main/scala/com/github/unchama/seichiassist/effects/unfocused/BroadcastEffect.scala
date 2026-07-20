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

      for {
        players <- IO {
          val players = Bukkit.getOnlinePlayers

          val snapshot: List[Player] = players.synchronized {
            import scala.jdk.CollectionConverters._
            players.asScala.toList
          }
          snapshot
        }
        _ <- players.traverse(effect.run)
      } yield ()
    }
}
