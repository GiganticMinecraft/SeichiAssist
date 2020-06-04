package com.github.unchama.generic.effect

import cats.effect.concurrent.Deferred
import cats.effect.{CancelToken, Concurrent, Fiber}

/**
 * We can think of a [[cats.effect.concurrent.Deferred]] as a "mutable" Promise to which
 * read and write operations are synchronized. Then [[cats.effect.concurrent.TryableDeferred]] is a
 * [[cats.effect.concurrent.Deferred]] which can immediately tell whether it has been completed or not.
 *
 * Analogously then, [[Fiber]] is a read-only promise and we should be able to describe
 * a [[Fiber]] which can tell its completion status. [[TryableFiber]] serves this purpose.
 */
trait TryableFiber[F[_], A] extends Fiber[F, A] {
  implicit val fConcurrent: Concurrent[F]

  /**
   * Obtains the current value of the `Fiber`, or None if it hasn't completed.
   */
  def tryJoin: F[Option[A]]

  import cats.implicits._

  def isComplete: F[Boolean] = tryJoin map (_.nonEmpty)

  def isIncomplete: F[Boolean] = isComplete map (!_)

  /**
   * A computation that cancels the fiber if it is not complete,
   * returning whether the cancellation happened or not.
   */
  def cancelIfIncomplete: F[Boolean] = {
    // cancellation does not alter completion status;
    // if it has been complete then nothing occurs,
    // or else cancellation is performed and tryJoin would be empty.
    cancel >> isComplete
  }
}

object TryableFiber {
  /**
   * Start concurrent execution of the source suspended in `F`.
   *
   * @return a [[TryableFiber]] which can be used to cancel, join or tryJoin the result
   */
  def start[F[_]: Concurrent, A](fa: F[A]): F[TryableFiber[F, A]] = {
    import cats.implicits._

    for {
      promise <- Deferred.tryable[F, A]
      fiber <- Concurrent[F].start(fa >>= promise.complete)
    } yield new TryableFiber[F, A] {
      override implicit val fConcurrent: Concurrent[F] = Concurrent[F]
      override def tryJoin: F[Option[A]] = promise.tryGet
      override def cancel: CancelToken[F] = fiber.cancel
      override def join: F[A] = promise.get
    }
  }
}
