package com.github.unchama.seichiassist.subsystems.dragonnighttime.application

import cats.effect.{Async, Clock, Sync}
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

import java.time.ZoneId

object DragonNightTimeRoutine {
  def apply[F[_]: Async: CanBroadcast, G[_]: [f[_]] =>> ContextCoercion[f, F], Player](
    implicit fastDiggingEffectApi: FastDiggingEffectWriteApi[F, Player],
    manaApi: ManaApi[F, G, Player]
  ): F[Nothing] = {
    import cats.implicits._
    import scala.concurrent.duration.FiniteDuration

    val todayEffectivePeriod =
      Clock[F].realTimeInstant.map { instant =>
        val currentLocalDate = instant.atZone(ZoneId.systemDefault()).toLocalDate

        DragonNightTimeImpl.effectivePeriod(currentLocalDate)
      }

    val getIntervalToNextExecution: F[FiniteDuration] = for {
      effectivePeriod <- todayEffectivePeriod
      dailyDragonNightTime = effectivePeriod.startAt
      getIntervalToNextExecution <- {
        import cats.implicits._

        Clock[F].realTimeInstant.map { instant =>
          val currentLocalTime = instant.atZone(ZoneId.systemDefault()).toLocalTime

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
      _ <- CanBroadcast[F].broadcast(
        s"今から${DragonNightTimeImpl.endAtString(effectivePeriod)}までの間、採掘速度上昇Lv10のバフが付与され、マナ使用率が80%になります。"
      )
      _ <- Async[F].sleep(effectivePeriod.toFiniteDuration)
      _ <- ContextCoercion(manaApi.setManaConsumptionWithDragonNightTime(ManaMultiplier(1)))
      _ <- CanBroadcast[F].broadcast("ドラゲナイタイムが終了しました。")
    } yield ()

    RepeatingRoutine.permanentRoutine(getIntervalToNextExecution, routineAction)
  }
}
