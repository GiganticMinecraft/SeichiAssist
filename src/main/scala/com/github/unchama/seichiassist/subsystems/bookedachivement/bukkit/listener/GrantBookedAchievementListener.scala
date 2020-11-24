package com.github.unchama.seichiassist.subsystems.bookedachivement.bukkit.listener

import cats.effect.ConcurrentEffect
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.bookedachivement.domain.AchievementOperation
import com.github.unchama.seichiassist.subsystems.bookedachivement.service.AchievementBookingService
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}

class GrantBookedAchievementListener[F[_] : ConcurrentEffect](implicit
  effectEnvironment: EffectEnvironment,
  service: AchievementBookingService[F]
) extends Listener {

  import cats.effect.implicits._
  import cats.implicits._

  @EventHandler
  def onPlayerJoin(playerJoinEvent: PlayerJoinEvent): Unit = {
    val player = playerJoinEvent.getPlayer
    val uuid = player.getUniqueId
    val playerData = SeichiAssist.playermap(uuid)

    val effectRunner = Bukkit.getConsoleSender
    val program = for {
      ids <- service.loadBookedAchievementsIds(player.getUniqueId).toIO
      _ <- ids.map {
        case (AchievementOperation.GIVE, id) =>
          playerData.tryForcefullyUnlockAchievement(id).run(effectRunner)
        case (AchievementOperation.DEPRIVE, id) =>
          playerData.forcefullyDepriveAchievement(id).run(effectRunner)
      }.sequence
    } yield ()

    effectEnvironment.runEffectAsync("未受け取りの予約実績を読み込み、付与する", program)
  }
}
