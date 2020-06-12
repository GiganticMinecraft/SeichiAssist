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

  /**
   * 初期状態 `init` から、`getInterval` によってスリープしたのち `action` にて
   * 状態を副作用付きで更新するという操作を、 `action` がNoneを返すまで繰り返す
   * @tparam State ループにて保持される状態の型
   * @return
   */
  def recMTask[State](init: State)(action: State => IO[Option[State]])
                     (getInterval: IO[FiniteDuration])(implicit context: RepeatingTaskContext): IO[Unit] = {
    val recoveringAction: State => IO[Option[State]] = a =>
      SyncExtra.recoverWithStackTrace("繰り返し実行タスクの実行に失敗しました", None, action(a))

    Monad[IO].tailRecM(init) { state =>
      sleepWith(getInterval) >> recoveringAction(state) map {
        case Some(value) => Left(value)
        case None => Right(())
      }
    }
  }

}
