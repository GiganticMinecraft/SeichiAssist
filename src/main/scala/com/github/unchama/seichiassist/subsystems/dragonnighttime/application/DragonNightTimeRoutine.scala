package com.github.unchama.seichiassist.subsystems.dragonnighttime.application

import cats.effect.{Concurrent, Sync, Timer}
import com.github.unchama.concurrent.RepeatingRoutine
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.FastDiggingEffectWriteApi
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.{FastDiggingAmplifier, FastDiggingEffect, FastDiggingEffectCause}
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.mana.domain.ManaMultiplier
import com.github.unchama.util.time.LocalTimeUtil

import java.time.{Instant, LocalDateTime, ZoneId}
import java.util.concurrent.TimeUnit

object DragonNightTimeRoutine {
  def apply[F[_]: Concurrent: CanBroadcast: Timer, G[_]: ContextCoercion[*[_], F], Player](
    implicit fastDiggingEffectApi: FastDiggingEffectWriteApi[F, Player],
    manaApi: ManaApi[F, G, Player]
  ): F[Nothing] = {
    import cats.implicits._
    import scala.concurrent.duration.FiniteDuration

    val todayEffectivePeriod =
      Timer[F].clock.realTime(TimeUnit.MILLISECONDS).map { currentEpochMilli =>
        val currentLocalDate =
          LocalDateTime
            .ofInstant(Instant.ofEpochMilli(currentEpochMilli), ZoneId.systemDefault())
            .toLocalDate

        DragonNightTimeImpl.effectivePeriod(currentLocalDate)
      }

    val getIntervalToNextExecution: F[FiniteDuration] = for {
      effectivePeriod <- todayEffectivePeriod
      dailyDragonNightTime = effectivePeriod.startAt
      getIntervalToNextExecution <- {
        import cats.implicits._

        Timer[F].clock.realTime(TimeUnit.MILLISECONDS).map { currentEpochMilli =>
          val currentLocalTime =
            LocalDateTime
              .ofInstant(Instant.ofEpochMilli(currentEpochMilli), ZoneId.systemDefault())
              .toLocalTime

          LocalTimeUtil.getDurationToNextTimeOfDay(currentLocalTime, dailyDragonNightTime)
        }
      }
    } yield getIntervalToNextExecution

    val routineAction: F[Unit] = for {
      effectivePeriod <- todayEffectivePeriod
      effectToAdd <- Sync[F].pure(
        FastDiggingEffect(
          FastDiggingAmplifier(10.0),
          FastDiggingEffectCause.FromDragonNightTime
        )
      )
      _ <- fastDiggingEffectApi.addEffectToAllPlayers(
        effectToAdd,
        effectivePeriod.toFiniteDuration
      )
      _ <- ContextCoercion(manaApi.setManaConsumptionWithDragonNightTime(ManaMultiplier(0.8)))
      _ <- CanBroadcast[F].broadcast("ドラゲナイタイム開始！")
      _ <- CanBroadcast[F].broadcast("採掘速度上昇Lv10のバフが1時間付与され、マナ使用率が80%になりました")
      _ <- Timer[F].sleep(effectivePeriod.toFiniteDuration)
      _ <- ContextCoercion(manaApi.setManaConsumptionWithDragonNightTime(ManaMultiplier(1)))
      _ <- CanBroadcast[F].broadcast("ドラゲナイタイムが終了しました。")
    } yield ()

    RepeatingRoutine.permanentRoutine(getIntervalToNextExecution, routineAction)
  }
}
