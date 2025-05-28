package com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit

import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.entity.Player

import java.time.LocalDateTime
import java.time.Instant
import java.time.ZoneId
import cats.effect.{Effect, Sync, Timer}
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.subsystems.dragonnighttime.domain.Period
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.FastDiggingEffectWriteApi
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.{
  FastDiggingAmplifier,
  FastDiggingEffect,
  FastDiggingEffectCause
}
import com.github.unchama.targetedeffect.commandsender.MessageEffectF

import java.util.concurrent.TimeUnit

class JoinListener[F[_]: Effect: Timer](
  implicit fastDiggingEffectApi: FastDiggingEffectWriteApi[F, Player],
  effectEnvironment: EffectEnvironment
) extends Listener {

  import cats.implicits._

  @EventHandler
  def onJoin(e: PlayerJoinEvent): Unit = {
    val program = for {
      currentLocalTime <- Timer[F].clock.realTime(TimeUnit.MILLISECONDS).map {
        currentEpochMilli =>
          LocalDateTime
            .ofInstant(Instant.ofEpochMilli(currentEpochMilli), ZoneId.systemDefault())
            .toLocalTime
      }
      isDragonNightTime <- Sync[F].pure(Period.effectivePeriod.contains(currentLocalTime))
      effectToAdd <-
        Sync[F].pure(
          FastDiggingEffect(
            FastDiggingAmplifier(10.0),
            FastDiggingEffectCause.FromDragonNightTime
          )
        )
      remainingDuration <- Sync[F].pure(
        Period.effectivePeriod.remainingDuration(currentLocalTime)
      )
      _ <- remainingDuration
        .traverse(duration =>
          fastDiggingEffectApi
            .addEffect(effectToAdd, duration)(e.getPlayer) >> MessageEffectF[F]("")
            .apply(e.getPlayer)
        )
        .whenA(isDragonNightTime)
    } yield ()

    effectEnvironment.unsafeRunEffectAsync("ドラゲナイタイムかチェックし、そうならエフェクトを付与する", program)

  }
}
