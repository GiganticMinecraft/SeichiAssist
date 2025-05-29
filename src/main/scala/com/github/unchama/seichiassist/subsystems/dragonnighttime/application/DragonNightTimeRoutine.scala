package com.github.unchama.seichiassist.subsystems.dragonnighttime.application

import cats.effect.{Concurrent, Timer}
import com.github.unchama.concurrent.RepeatingRoutine
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.FastDiggingEffectWriteApi
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.{
  FastDiggingAmplifier,
  FastDiggingEffect,
  FastDiggingEffectCause
}
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
    import scala.concurrent.duration._

    val todayEffectivePeriod =
      Timer[F].clock.realTime(TimeUnit.MILLISECONDS).map { currentEpochMilli =>
        val currentLocalDate =
          LocalDateTime
            .ofInstant(Instant.ofEpochMilli(currentEpochMilli), ZoneId.systemDefault())
            .toLocalDate

        DragonNightTimeImpl.effectivePeriod(currentLocalDate)
      }

    val dailyDragonNightTime = todayEffectivePeriod.map(_.startAt)

    val effectToAdd =
      FastDiggingEffect(FastDiggingAmplifier(10.0), FastDiggingEffectCause.FromDragonNightTime)

    val getIntervalToNextExecution: F[FiniteDuration] = {
      import cats.implicits._

      Timer[F].clock.realTime(TimeUnit.MILLISECONDS).flatMap { currentEpochMilli =>
        val currentLocalTime =
          LocalDateTime
            .ofInstant(Instant.ofEpochMilli(currentEpochMilli), ZoneId.systemDefault())
            .toLocalTime

        dailyDragonNightTime.map(LocalTimeUtil.getDurationToNextTimeOfDay(currentLocalTime, _))
      }
    }

    val routineAction: F[Unit] = {
      todayEffectivePeriod.flatMap { effectivePeriod =>
        fastDiggingEffectApi.addEffectToAllPlayers(
          effectToAdd,
          effectivePeriod.toFiniteDuration
        ) >>
          ContextCoercion(manaApi.setManaConsumptionWithDragonNightTime(ManaMultiplier(0.8))) >>
          CanBroadcast[F].broadcast("ドラゲナイタイム開始！") >>
          CanBroadcast[F].broadcast("採掘速度上昇Lv10のバフが1時間付与され、マナ使用率が80%になりました") >>
          Timer[F].sleep(effectivePeriod.toFiniteDuration) >>
          ContextCoercion(manaApi.setManaConsumptionWithDragonNightTime(ManaMultiplier(1))) >>
          CanBroadcast[F].broadcast("ドラゲナイタイムが終了しました。")
      }
    }

    RepeatingRoutine.permanentRoutine(getIntervalToNextExecution, routineAction)
  }
}
