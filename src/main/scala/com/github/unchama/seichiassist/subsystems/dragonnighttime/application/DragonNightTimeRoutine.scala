package com.github.unchama.seichiassist.subsystems.dragonnighttime.application

import java.time.LocalTime

import cats.Applicative
import cats.effect.{Sync, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}

object DragonNightTimeRoutine {
  def apply[F[_] : Sync : CanAddEffect : CanNotifyStart : Timer]()(implicit context: RepeatingTaskContext): F[Nothing] = {
    import cats.implicits._

    import scala.concurrent.duration._

    val getRepeatInterval: F[FiniteDuration] = Applicative[F].pure(1.seconds)

    val time = LocalTime.of(20, 0, 0)

    val routineAction: F[Unit] =
      if (LocalTime.now().equals(time))
        CanNotifyStart[F].notify("ドラゲナイタイム開始！") >>
          CanNotifyStart[F].notify("採掘速度上昇Lv10のバフが1時間付与されました") >>
          CanAddEffect[F].addEffect
      else
        Applicative[F].unit

    RepeatingRoutine.permanentRoutine(getRepeatInterval, routineAction)
  }
}
