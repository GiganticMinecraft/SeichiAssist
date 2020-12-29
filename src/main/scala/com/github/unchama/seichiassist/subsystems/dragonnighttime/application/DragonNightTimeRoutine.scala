package com.github.unchama.seichiassist.subsystems.dragonnighttime.application

import java.time.{Instant, LocalDateTime, LocalTime, ZoneId}
import java.util.concurrent.TimeUnit

import cats.effect.{Sync, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.util.time.LocalTimeUtil

object DragonNightTimeRoutine {
  def apply[F[_] : Sync : CanAddEffect : Notifiable : Timer]()(implicit context: RepeatingTaskContext): F[Nothing] = {
    import cats.implicits._

    import scala.concurrent.duration._

    val dailyDragonNightTime = LocalTime.of(20, 0, 0)

    val getRepeatInterval: F[FiniteDuration] = {
      import cats.implicits._

      Timer[F].clock.realTime(TimeUnit.MILLISECONDS).map { currentEpochMilli =>
        val currentLocalTime =
          LocalDateTime.ofInstant(
            Instant.ofEpochMilli(currentEpochMilli),
            ZoneId.systemDefault()
          ).toLocalTime
        LocalTimeUtil.getDurationToNextTimeOfDay(currentLocalTime, dailyDragonNightTime)
      }
    }

    val routineAction: F[Unit] =
      Notifiable[F].notify("ドラゲナイタイム開始！") >>
        Notifiable[F].notify("採掘速度上昇Lv10のバフが1時間付与されました") >>
        CanAddEffect[F].addEffect

    RepeatingRoutine.permanentRoutine(getRepeatInterval, routineAction)
  }
}
