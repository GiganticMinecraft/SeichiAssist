package com.github.unchama.seichiassist.subsystems.dragonnighttime.application

import cats.effect.{Concurrent, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.mana.domain.ManaMultiplier
import com.github.unchama.util.time.LocalTimeUtil

import java.time.{Instant, LocalDateTime, LocalTime, ZoneId}
import java.util.concurrent.TimeUnit

object DragonNightTimeRoutine {
  def apply[
    F[_] : Concurrent : AddableWithContext : Notifiable : Timer,
    G[_] : ContextCoercion[*[_], F], Player
  ](implicit
    context: RepeatingTaskContext,
    manaApi: ManaApi[F, G, Player]): F[Nothing] = {
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

    val routineAction: F[Unit] = {
      val manipulateManaMultiplier =
        ContextCoercion(manaApi.setGlobalManaMultiplier(ManaMultiplier(0.8))) >>
          Timer[F].sleep(1.hour) >>
          ContextCoercion(manaApi.setGlobalManaMultiplier(ManaMultiplier(1)))

      Notifiable[F].notify("ドラゲナイタイム開始！") >>
        Notifiable[F].notify("採掘速度上昇Lv10のバフが1時間付与され、マナ使用率が80%になりました") >>
        AddableWithContext[F].addEffect >>
        Concurrent[F].start(manipulateManaMultiplier).as(())
    }

    RepeatingRoutine.permanentRoutine(getRepeatInterval, routineAction)
  }
}
