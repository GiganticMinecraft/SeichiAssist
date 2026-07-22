package com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit

import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.entity.Player

import java.time.ZoneId
import cats.effect.{Async, Clock, Sync}
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.subsystems.dragonnighttime.application.DragonNightTimeImpl
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.FastDiggingEffectWriteApi
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.{
  FastDiggingAmplifier,
  FastDiggingEffect,
  FastDiggingEffectCause
}
import com.github.unchama.targetedeffect.commandsender.MessageEffectF

import java.time.format.DateTimeFormatter
import cats.effect.Temporal

class JoinListener[F[_]: Async: Temporal](
  implicit fastDiggingEffectApi: FastDiggingEffectWriteApi[F, Player],
  effectEnvironment: EffectEnvironment[F]
) extends Listener {

  import cats.implicits._

  @EventHandler
  def onJoin(e: PlayerJoinEvent): Unit = {
    val program = for {
      currentLocalDate <- Clock[F]
        .realTimeInstant
        .map(_.atZone(ZoneId.systemDefault()).toLocalDate)
      effectivePeriod <- Sync[F].pure(DragonNightTimeImpl.effectivePeriod(currentLocalDate))
      currentLocalTime <- Clock[F]
        .realTimeInstant
        .map(_.atZone(ZoneId.systemDefault()).toLocalTime)
      isDragonNightTime <- Sync[F].pure(effectivePeriod.contains(currentLocalTime))
      effectToAdd <-
        Sync[F].pure(
          FastDiggingEffect(
            FastDiggingAmplifier(10.0),
            FastDiggingEffectCause.FromDragonNightTime
          )
        )
      remainingDuration <- Sync[F].pure(effectivePeriod.remainingDuration(currentLocalTime))
      _ <- remainingDuration
        .traverse(duration =>
          fastDiggingEffectApi
            .addEffect(effectToAdd, duration)(e.getPlayer) >> MessageEffectF[F](
            s"採掘速度上昇Lv10のバフが${effectivePeriod.endAt.format(DateTimeFormatter.ofPattern("HH時mm分"))}まで付与され、マナ使用率が80%になりました"
          ).apply(e.getPlayer)
        )
        .whenA(isDragonNightTime)
    } yield ()

    effectEnvironment.unsafeRunEffectAsync("ドラゲナイタイムかチェックし、そうならエフェクトを付与する", program)

  }
}
