package com.github.unchama.concurrent

import cats.Monad
import cats.effect.{Sync, Timer}
import com.github.unchama.generic.effect.SyncExtra

import scala.concurrent.duration.FiniteDuration

object RepeatingRoutine {

  import cats.implicits._

  private def sleepWith[F[_] : Timer : Monad](getInterval: F[FiniteDuration]): F[Unit] =
    getInterval >>= (Timer[F].sleep(_))

  def permanentRoutine[F[_] : Timer : Sync, U](getInterval: F[FiniteDuration], action: F[U]): F[Nothing] = {
    val recoveringAction = SyncExtra.recoverWithStackTrace("定期実行タスクの実行に失敗しました", (), action.as(()))

    Monad[F].foreverM(sleepWith(getInterval) >> recoveringAction)
  }

  /**
   * 初期状態 `init` から、`getInterval` によってスリープしたのち `action` にて
   * 状態を副作用付きで更新するという操作を、 `action` がNoneを返すまで繰り返す
   *
   * @tparam State ループにて保持される状態の型
   * @return
   */
  def recMTask[F[_] : Timer : Sync, State](init: State)(action: State => F[Option[State]])
                                          (getInterval: F[FiniteDuration])(implicit context: RepeatingTaskContext): F[Unit] = {
    val recoveringAction: State => F[Option[State]] = a =>
      SyncExtra.recoverWithStackTrace("繰り返し実行タスクの実行に失敗しました", None, action(a))

    Monad[F].tailRecM(init) { state =>
      sleepWith(getInterval) >> recoveringAction(state) map {
        case Some(value) => Left(value)
        case None => Right(())
      }
    }
  }

}
