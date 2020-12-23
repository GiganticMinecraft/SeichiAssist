package com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary

import java.time.LocalDate

import cats.effect.{ConcurrentEffect, IO, LiftIO}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.DefaultEffectEnvironment
import com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary.Anniversary.{ANNIVERSARY_COUNT, EVENT_DATE, blogArticleUrl}
import com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary.AnniversaryItemData.mineHead
import com.github.unchama.seichiassist.subsystems.seasonalevents.service.LastQuitInquiringService
import com.github.unchama.seichiassist.util.Util.grantItemStacksEffect
import com.github.unchama.targetedeffect.TargetedEffect.emptyEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}

class AnniversaryListener[F[_] : ConcurrentEffect : NonServerThreadContextShift](implicit
    effectEnvironment: EffectEnvironment,
    service: LastQuitInquiringService[F]) extends Listener {
  @EventHandler
  def onPlayerJoin(event: PlayerJoinEvent): Unit = {
    val player = event.getPlayer

    if (LocalDate.now().isEqual(EVENT_DATE)) {
      List(
        s"${BLUE}本日でギガンティック☆整地鯖は${ANNIVERSARY_COUNT}周年を迎えます。",
        s"${BLUE}これを記念し、限定アイテムを入手可能です。詳しくは下記URLのサイトをご覧ください。",
        s"$DARK_GREEN$UNDERLINE$blogArticleUrl"
      ).foreach(player.sendMessage)
      player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
    }
  }

  import cats.implicits._

  @EventHandler
  def onPlayerDeath(event: PlayerDeathEvent): Unit = {
    val player = event.getEntity

    val program = for {
      _ <- NonServerThreadContextShift[F].shift
      lastQuit <- service.loadLastQuitDateTime(player.getName)
      _ <- LiftIO[F].liftIO(
        IO {
          val hasNotJoinedInEventYet = lastQuit match {
            case Some(dateTime) => dateTime.isBefore(EVENT_DATE.atStartOfDay())
            case None => false
          }
          val effect = if (hasNotJoinedInEventYet) Set(
            grantItemStacksEffect(mineHead),
            MessageEffect(s"${BLUE}ギガンティック☆整地鯖${ANNIVERSARY_COUNT}周年の記念品を入手しました。"),
            FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f)
          ) else Set(emptyEffect)

          effect.foreach(_.run(player))
        }
      )
    } yield ()

    effectEnvironment.runEffectAsync(
      s"${ANNIVERSARY_COUNT}周年記念ヘッドを付与する",
      program
    )
  }
}