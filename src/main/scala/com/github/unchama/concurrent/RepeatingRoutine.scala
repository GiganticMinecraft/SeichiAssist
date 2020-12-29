package com.github.unchama.concurrent

import cats.effect.{Sync, Timer}
import cats.{Monad, MonadError}
import com.github.unchama.generic.{ApplicativeErrorThrowableExtra, WeakRef}
import io.chrisdavenport.log4cats.ErrorLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration.FiniteDuration

object RepeatingRoutine {

  import cats.implicits._

  private def sleepWith[F[_] : Timer : Monad](getInterval: F[FiniteDuration]): F[Unit] =
    getInterval >>= (Timer[F].sleep(_))

  def permanentRoutine[F[_] : Timer : Sync, U](getInterval: F[FiniteDuration], action: F[U]): F[Nothing] = {
    Slf4jLogger.create.flatMap[Nothing] { implicit logger =>
      foreverMRecovering[F, U, Nothing](action)(getInterval)
    }
  }

  def foreverMRecovering[
    F[_] : Timer : MonadError[*[_], Throwable] : ErrorLogger,
    U, R
  ](action: F[U])(getInterval: F[FiniteDuration]): F[R] = {
    val recoveringAction: F[Unit] =
      ApplicativeErrorThrowableExtra.recoverWithStackTrace(action.as(()))(
        "繰り返し実行タスクの実行に失敗しました",
        ()
      )

    Monad[F].foreverM(sleepWith(getInterval) >> recoveringAction)
  }

  def whileReferencedRecovering[
    F[_] : Timer : MonadError[*[_], Throwable] : ErrorLogger,
    R <: AnyRef, U
  ](reference: WeakRef[F, R], action: R => F[U], getInterval: F[FiniteDuration]): F[Unit] = {
    whileDefinedMRecovering(())(_ =>
      reference.get.flatMap[Option[Unit]] {
        case Some(value) => action(value).as(Some(()))
        case None => Monad[F].pure(None)
      }
    )(getInterval)
  }

  /**
   * 初期状態 `init` から、`getInterval` によってスリープしたのち `action` にて
   * 状態を副作用付きで更新するという操作を、 `action` がNoneを返すまで繰り返す
   *
   * よりロギングに関する制御を求める時は [[whileDefinedMRecovering]] を使うこと。
   *
   * @tparam State ループにて保持される状態の型
   * @return
   */
  def recMTask[F[_] : Timer : Sync, State](init: State)(action: State => F[Option[State]])
                                          (getInterval: F[FiniteDuration])(implicit context: RepeatingTaskContext): F[Unit] = {
    Slf4jLogger.create.flatMap(implicit logger =>
      whileDefinedMRecovering(init)(action)(getInterval)
    )
  }

  /**
   * 初期状態 `init` から、`getInterval` によってスリープしたのち `action` にて
   * 状態を副作用付きで更新するという操作を、 `action` がNoneを返すまで繰り返す
   *
   * @tparam State ループにて保持される状態の型
   * @return
   */
  def whileDefinedMRecovering[
    F[_] : MonadError[*[_], Throwable] : Timer : ErrorLogger,
    State
  ](init: State)(action: State => F[Option[State]])
   (getInterval: F[FiniteDuration]): F[Unit] = {
    val recoveringAction: State => F[Option[State]] = s =>
      ApplicativeErrorThrowableExtra.recoverWithStackTrace(action(s))(
        "繰り返し実行タスクの実行に失敗しました",
        None
      )

    Monad[F].tailRecM(init) { state =>
      sleepWith(getInterval) >> recoveringAction(state) map {
        case Some(value) => Left(value)
        case None => Right(())
      }
    }
  }

}
