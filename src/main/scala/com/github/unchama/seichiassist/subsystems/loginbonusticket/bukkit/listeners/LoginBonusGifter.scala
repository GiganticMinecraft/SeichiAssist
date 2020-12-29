package com.github.unchama.seichiassist.subsystems.loginbonusticket.bukkit.listeners

import java.time.LocalDate
import java.util.UUID

import cats.effect.{ConcurrentEffect, IO, LiftIO}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.DefaultEffectEnvironment
import com.github.unchama.seichiassist.subsystems.loginbonusticket.bukkit.itemstack.BukkitLoginBonusTicketItemStack.loginBonusTicket
import com.github.unchama.seichiassist.subsystems.seasonalevents.domain.LastQuitPersistenceRepository
import com.github.unchama.seichiassist.util.Util.grantItemStacksEffect
import com.github.unchama.targetedeffect.TargetedEffect.emptyEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.ChatColor.AQUA
import org.bukkit.Sound

class LoginBonusGifter[F[_] : ConcurrentEffect : NonServerThreadContextShift]
  (implicit effectEnvironment: EffectEnvironment, repository: LastQuitPersistenceRepository[F, UUID]) extends Listener {
  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    val player = event.getPlayer

    import cats.implicits._

    val program = for {
      _ <- NonServerThreadContextShift[F].shift
      lastQuit <- repository.loadPlayerLastQuit(player.getUniqueId)
      _ <- LiftIO[F].liftIO(IO{
        val hasNotJoinedToday = lastQuit match {
          case Some(dateTime) => dateTime.isBefore(LocalDate.now().atStartOfDay())
          case None => true
        }

        val effects =
          if (hasNotJoinedToday) List(
            grantItemStacksEffect(loginBonusTicket),
            MessageEffect(s"${AQUA}今日1回目のログインのため、ログインボーナスチケットを配布しました。"),
            FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f))
          else List(emptyEffect)

        effects.traverse(_.run(player))
      })
    } yield ()

    effectEnvironment.runEffectAsync("ログインボーナスチケットを付与する", program)
  }
}