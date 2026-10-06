package com.github.unchama.concurrent

import cats.effect.Async

import scala.concurrent.ExecutionContext

trait NonServerThreadContextShift[F[_]] {

  /**
   * `fa`の同期的な領域をMinecraftサーバースレッド以外で実行する。
   */
  def evalOn[A](fa: F[A]): F[A]

}

object NonServerThreadContextShift {

  def apply[F[_]](implicit cs: NonServerThreadContextShift[F]): NonServerThreadContextShift[F] =
    cs

  def fromExecutionContext[F[_]](
    executionContext: ExecutionContext
  )(implicit F: Async[F]): NonServerThreadContextShift[F] =
    new NonServerThreadContextShift[F] {
      override def evalOn[A](fa: F[A]): F[A] = F.evalOn(fa, executionContext)
    }

}
