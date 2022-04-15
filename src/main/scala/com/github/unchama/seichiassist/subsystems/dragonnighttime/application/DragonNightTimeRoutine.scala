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

import java.time.{Instant, LocalDateTime, LocalTime, ZoneId}
import java.util.concurrent.TimeUnit

object DragonNightTimeRoutine {
  def apply[F[_]: Concurrent: Notifiable: Timer, G[_]: ContextCoercion[*[_], F], Player](
    implicit fastDiggingEffectApi: FastDiggingEffectWriteApi[F, Player],
    manaApi: ManaApi[F, G, Player]
  ): F[Nothing] = {

    import cats.implicits._

    import scala.concurrent.duration._

    val dailyDragonNightTime = LocalTime.of(20, 0, 0)

    val effectToAdd =
      FastDiggingEffect(FastDiggingAmplifier(10.0), FastDiggingEffectCause.FromDragonNightTime)

    val getRepeatInterval: F[FiniteDuration] = {
      import cats.implicits._

      Timer[F].clock.realTime(TimeUnit.MILLISECONDS).map { currentEpochMilli =>
        val currentLocalTime =
          LocalDateTime
            .ofInstant(Instant.ofEpochMilli(currentEpochMilli), ZoneId.systemDefault())
            .toLocalTime
        LocalTimeUtil.getDurationToNextTimeOfDay(currentLocalTime, dailyDragonNightTime)
      }
    }

    val routineAction: F[Unit] = {
      val temporaryManaConsumingRateModifyTask = Concurrent[F]
        .start {
          ContextCoercion(manaApi.setGlobalManaMultiplier(ManaMultiplier(0.8))) >>
            Timer[F].sleep(1.hour) >>
            ContextCoercion(manaApi.setGlobalManaMultiplier(ManaMultiplier(1)))
        }
        .as(())

      Notifiable[F].notify("ドラゲナイタイム開始！") >>
        Notifiable[F].notify("採掘速度上昇Lv10のバフが1時間付与され、マナ使用率が80%になりました") >>
        fastDiggingEffectApi.addEffectToAllPlayers(effectToAdd, 1.hour) >>
        temporaryManaConsumingRateModifyTask.flatMap(_ => Notifiable[F].notify("ドラゲナイタイムが終了しました"))
    }

    RepeatingRoutine.permanentRoutine(getRepeatInterval, routineAction)
  }
}
