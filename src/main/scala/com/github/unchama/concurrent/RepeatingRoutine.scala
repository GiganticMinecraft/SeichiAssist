package com.github.unchama.concurrent

import cats.Monad
import cats.effect.IO
import com.github.unchama.generic.effect.SyncExtra

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

object RepeatingRoutine {
  import cats.implicits._

  private def sleepWith(getInterval: IO[FiniteDuration])(implicit ctx: ExecutionContext): IO[Unit] =
    getInterval >>= (IO.timer(ctx).sleep(_))

  def permanentRoutine(getInterval: IO[FiniteDuration], action: IO[Any])
                      (implicit context: RepeatingTaskContext): IO[Nothing] = {
    val recoveringAction = SyncExtra.recoverWithStackTrace("定期実行タスクの実行に失敗しました", (), action)

    Monad[IO].foreverM(sleepWith(getInterval) >> recoveringAction)
  }

  def loopingRoutine(getInterval: IO[FiniteDuration], action: IO[Boolean])
                    (implicit context: RepeatingTaskContext): IO[Unit] = {
    val recoveringAction = SyncExtra.recoverWithStackTrace("繰り返し実行タスクの実行に失敗しました", false, action)

    Monad[IO].iterateWhile(sleepWith(getInterval) >> recoveringAction)(identity).as(())
  }
}