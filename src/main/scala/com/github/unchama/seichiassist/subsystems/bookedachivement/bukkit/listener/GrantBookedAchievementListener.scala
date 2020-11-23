package com.github.unchama.seichiassist.subsystems.bookedachivement.bukkit.listener

import cats.data.EitherT
import cats.effect.ConcurrentEffect
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.bookedachivement.domain.AchievementOperation
import com.github.unchama.seichiassist.subsystems.bookedachivement.service.AchievementBookingService
import com.github.unchama.targetedeffect.SequentialEffect
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}

class GrantBookedAchievementListener[AsyncContext[_] : ConcurrentEffect](implicit
  effectEnvironment: EffectEnvironment,
  service: AchievementBookingService[AsyncContext]
) extends Listener {

  import cats.effect.implicits._

  @EventHandler
  def onPlayerJoin(playerJoinEvent: PlayerJoinEvent): Unit = {
    val player = playerJoinEvent.getPlayer
    val uuid = player.getUniqueId
    val playerData = SeichiAssist.playermap(uuid)

    effectEnvironment.runEffectAsync("未受け取りの予約実績を読み込み付与する",
      {
        for {
          ids <- EitherT(service.loadBookedAchievementsIds(player.getUniqueId))
        } yield {
          val effects = for {
            (action, id) <- ids
          } yield action match {
            case AchievementOperation.GIVE => playerData.tryForcefullyUnlockAchievement(id)
            case AchievementOperation.DEPRIVE => playerData.forcefullyDepriveAchievement(id)
          }
          effectEnvironment.runEffectAsync(
            "実績の付与・剥奪を行う",
            SequentialEffect(effects).run(player)
          )
        }
      }
        .value
        .start
    )
  }
}
