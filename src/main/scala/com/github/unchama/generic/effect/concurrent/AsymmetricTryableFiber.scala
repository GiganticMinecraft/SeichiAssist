package com.github.unchama.generic.effect.concurrent

import cats.effect.{CancelToken, Concurrent, Fiber, Sync}
import cats.{FlatMap, Monad}
import com.github.unchama.generic.ContextCoercion

/**
 * We can think of a [[cats.effect.concurrent.Deferred]] as a "mutable" Promise to which
 * read and write operations are synchronized. Then [[cats.effect.concurrent.TryableDeferred]] is a
 * [[cats.effect.concurrent.Deferred]] which can immediately tell whether it has been completed or not.
 *
 * Analogously then, [[Fiber]] is a read-only promise and we should be able to describe
 * a [[Fiber]] which can tell its completion status. [[AsymmetricTryableFiber]] serves this purpose.
 *
 * @tparam F the context in which the fiber is run
 */
trait AsymmetricTryableFiber[F[_], A] extends Fiber[F, A] {
  implicit val fFMap: FlatMap[F]

  /**
   * Obtains the current value of the `Fiber`, or None if it hasn't completed.
   */
  def tryJoin[G[_] : Sync]: G[Option[A]]

  import ContextCoercion._
  import cats.implicits._

  def isComplete[G[_] : Sync]: G[Boolean] = tryJoin[G] map (_.nonEmpty)

  def isIncomplete[G[_] : Sync]: G[Boolean] = isComplete map (!_)

  /**
   * A computation that cancels the fiber if it is not complete,
   * returning whether the cancellation happened or not.
   */
  def cancelIfIncomplete[G[_] : Sync : ContextCoercion[*[_], F]]: F[Boolean] = {
    // cancellation does not alter completion status;
    // if it has been complete then nothing occurs,
    // or else cancellation is performed and tryJoin would be empty.
    cancel >> isIncomplete[G].coerceTo[F]
  }
}

object AsymmetricTryableFiber {
  /**
   * Start concurrent execution of the source suspended in `F`.
   *
   * @return a [[AsymmetricTryableFiber]] which can be used to cancel, join or tryJoin the result
   */
  def start[F[_], A](fa: F[A])
                    (implicit fConc: Concurrent[F]): F[AsymmetricTryableFiber[F, A]] = {
    import cats.implicits._

    for {
      promise <- AsymmetricTryableDeferred.concurrent[F, A]
      fiber <- fConc.start(fa >>= promise.complete)
    } yield new AsymmetricTryableFiber[F, A] {
      override implicit val fFMap: Concurrent[F] = fConc

      override def tryJoin[G[_] : Sync]: G[Option[A]] = promise.tryGet[G]

      override def cancel: CancelToken[F] = fiber.cancel

      override def join: F[A] = promise.get
    }
  }

  /**
   * Creates a trivial value of TryableFiber which is always complete.
   */
  def unit[F[_]](implicit F: Monad[F]): AsymmetricTryableFiber[F, Unit] = {
    new AsymmetricTryableFiber[F, Unit] {
      override implicit val fFMap: Monad[F] = F

      override def tryJoin[G[_] : Sync]: G[Option[Unit]] = Sync[G].pure(Some(()))

      override def cancel: CancelToken[F] = F.unit

      override def join: F[Unit] = F.unit
    }
  }
}
