package com.github.unchama.generic.effect.concurrent

import cats.effect.{Sync, Timer}
import cats.effect.concurrent.Ref

import scala.concurrent.duration.FiniteDuration
import com.github.unchama.concurrent.RepeatingRoutine

trait CachedRef[F[_], A] {

  import cats.implicits._

  val initial: Ref[F, A]

  /**
   * `A` の値を `F` のコンテキストで読み出す
   */
  final def read: F[A] = initial.get

  /**
   * CachedRef の値 `A` を更新する間隔
   */
  val updateInterval: F[FiniteDuration]

  /**
   * `updateInterval` の間隔で Ref の更新を行う
   */
  final def startUpdateRoutine(fa: F[A])(implicit F: Timer[F], sync: Sync[F]): F[Nothing] =
    RepeatingRoutine.permanentRoutine(updateInterval, fa.flatMap(initial.set))
}
