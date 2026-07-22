package com.github.unchama.seichiassist.subsystems.bookedachivement.bukkit.listener

import cats.effect.{Async, IO}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.bookedachivement.domain.AchievementOperation
import com.github.unchama.seichiassist.subsystems.bookedachivement.service.AchievementBookingService
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}

class GrantBookedAchievementListener[
  F[_]: Async: NonServerThreadContextShift: [f[_]] =>> ContextCoercion[IO, f]
](implicit effectEnvironment: EffectEnvironment[F], service: AchievementBookingService[F])
    extends Listener {

  import cats.implicits._

  @EventHandler
  def onPlayerJoin(playerJoinEvent: PlayerJoinEvent): Unit = {
    val player = playerJoinEvent.getPlayer
    val uuid = player.getUniqueId
    val playerData = SeichiAssist.playermap(uuid)

    val effectRunner = Bukkit.getConsoleSender

    /**
     * `.shift`を行ってから書き込みを行うわずかな時間にプレイヤーが退出し終了処理が行われる可能性があるが、 パフォーマンスを取り、妥協してこの実装としている。
     */
    val program = for {
      ids <- service.loadBookedAchievementsIds(player.getUniqueId)
      _ <- ContextCoercion[IO, F, List[Unit]](ids.traverse {
        case (AchievementOperation.GIVE, id) =>
          playerData.tryForcefullyUnlockAchievement(id).run(effectRunner)
        case (AchievementOperation.DEPRIVE, id) =>
          playerData.forcefullyDepriveAchievement(id).run(effectRunner)
      })
    } yield ()

    effectEnvironment.unsafeRunEffectAsync("未受け取りの予約実績を読み込み、付与する", program)
  }
}
