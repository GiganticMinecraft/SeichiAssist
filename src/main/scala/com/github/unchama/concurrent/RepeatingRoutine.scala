package com.github.unchama.concurrent

import cats.effect.Async
import cats.Monad
import com.github.unchama.generic.{ApplicativeErrorThrowableExtra}
import org.typelevel.log4cats.ErrorLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration.FiniteDuration
import cats.effect.Temporal

object RepeatingRoutine {

  import cats.implicits._

  private def sleepWith[F[_]: Temporal](
    getIntervalToNextExecution: F[FiniteDuration]
  ): F[Unit] =
    getIntervalToNextExecution >>= (Temporal[F].sleep(_))

  def permanentRoutine[F[_]: Async, U](
    getInterval: F[FiniteDuration],
    action: F[U]
  ): F[Nothing] = {
    Slf4jLogger.create.flatMap[Nothing] { implicit logger =>
      foreverMRecovering[F, U, Nothing](action)(getInterval)
    }
  }

  def foreverMRecovering[F[_]: Temporal: ErrorLogger, U, R](
    action: F[U]
  )(getIntervalToNextExecution: F[FiniteDuration]): F[R] = {
    val recoveringAction: F[Unit] =
      ApplicativeErrorThrowableExtra
        .recoverWithStackTrace(action.as(()))("繰り返し実行タスクの実行に失敗しました", ())

    Monad[F].foreverM(sleepWith(getIntervalToNextExecution) >> recoveringAction)
  }

  /**
   * 初期状態 `init` から、`getInterval` によってスリープしたのち `action` にて 状態を副作用付きで更新するという操作を、 `action`
   * がNoneを返すまで繰り返す
   *
   * よりロギングに関する制御を求める時は [[whileDefinedMRecovering]] を使うこと。
   *
   * @tparam State
   *   ループにて保持される状態の型
   * @return
   */
  def recMTask[F[_]: Async, State](
    init: State
  )(action: State => F[Option[State]])(getInterval: F[FiniteDuration]): F[Unit] = {
    Slf4jLogger
      .create
      .flatMap(implicit logger => whileDefinedMRecovering(init)(action)(getInterval))
  }

  /**
   * 初期状態 `init` から、`getInterval` によってスリープしたのち `action` にて 状態を副作用付きで更新するという操作を、 `action`
   * がNoneを返すまで繰り返す
   *
   * @tparam State
   *   ループにて保持される状態の型
   * @return
   */
  def whileDefinedMRecovering[F[_]: Temporal: ErrorLogger, State](init: State)(
    action: State => F[Option[State]]
  )(getIntervalToNextExecution: F[FiniteDuration]): F[Unit] = {
    val recoveringAction: State => F[Option[State]] = s =>
      ApplicativeErrorThrowableExtra
        .recoverWithStackTrace(action(s))("繰り返し実行タスクの実行に失敗しました", None)

    Monad[F].tailRecM(init) { state =>
      sleepWith(getIntervalToNextExecution) >> recoveringAction(state) map {
        case Some(value) => Left(value)
        case None        => Right(())
      }
    }
  }

}
