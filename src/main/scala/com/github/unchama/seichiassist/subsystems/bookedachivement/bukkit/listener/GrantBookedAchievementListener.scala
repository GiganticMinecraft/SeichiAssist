package com.github.unchama.seichiassist.subsystems.bookedachivement.bukkit.listener

import cats.effect.{SyncEffect, SyncIO}
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.bookedachivement.domain.AchievementOperation
import com.github.unchama.seichiassist.subsystems.bookedachivement.service.AchievementBookingService
import com.github.unchama.targetedeffect.SequentialEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}

class GrantBookedAchievementListener[SyncContext[_] : SyncEffect](implicit
  effectEnvironment: EffectEnvironment,
  service: AchievementBookingService[SyncContext]
) extends Listener {

  import cats.effect.implicits._

  @EventHandler
  def onPlayerJoin(playerJoinEvent: PlayerJoinEvent): Unit = {
    val player = playerJoinEvent.getPlayer
    val uuid = player.getUniqueId
    val playerData = SeichiAssist.playermap(uuid)

    val effect = service.loadBookedAchievementsIds(player.getUniqueId)
      .runSync[SyncIO]
      .unsafeRunSync() match {
      case Left(errorMessage) =>
        MessageEffect(errorMessage)
      case Right(ids) =>
        val idMap = ids.toMap
        val (shouldBeGivenIds, shouldBeDeprivedIds) =
          (idMap.get(AchievementOperation.GIVE), idMap.get(AchievementOperation.DEPRIVE))
        SequentialEffect(
          shouldBeGivenIds.map(playerData.tryForcefullyUnlockAchievement).toList ++
          shouldBeDeprivedIds.map(playerData.forcefullyDepriveAchievement)
        )
    }

    effectEnvironment.runEffectAsync("未受け取りの予約実績を付与する", effect.run(player))
  }
}
