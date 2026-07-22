package com.github.unchama.generic.effect.concurrent

import cats.effect.Async

import scala.concurrent.duration.FiniteDuration
import com.github.unchama.concurrent.RepeatingRoutine
import cats.effect.Ref

trait CachedRef[F[_], A] {

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
  final def startUpdateRoutine(fa: F[A])(implicit F: Async[F]): F[Nothing] =
    RepeatingRoutine.permanentRoutine(updateInterval, F.flatMap(fa)(initial.set))
}
