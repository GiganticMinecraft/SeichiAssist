package com.github.unchama.seichiassist.subsystems.bookedachivement.bukkit.listener

import cats.data.EitherT
import cats.effect.IO
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.command.CommandSender

class GrantBookedAchievementListener(implicit effectEnvironment: EffectEnvironment) extends Listener {
  @EventHandler
  def onPlayerJoin(playerJoinEvent: PlayerJoinEvent): Unit = {
    val player = playerJoinEvent.getPlayer
    val uuid = player.getUniqueId
    val playerData = SeichiAssist.playermap(uuid)

    val manipulator = SeichiAssist.databaseGateway.bookedAchievementManipulator
    import manipulator._

    val effect = {
      for {
        bookedAchievementIds <- EitherT(loadNotGivenBookedAchievementsOf(player))
        _ <- EitherT.right[TargetedEffect[CommandSender]](
          IO {
            bookedAchievementIds
              .foreach(playerData.tryForcefullyUnlockAchievement)
          }
        )
        _ <- EitherT(makeAllOfAchivementsReceived(player))
      } yield ()
    }.getOrElse(return)

    effectEnvironment.runEffectAsync("未受け取りの予約実績を取得する", effect)
  }
}
