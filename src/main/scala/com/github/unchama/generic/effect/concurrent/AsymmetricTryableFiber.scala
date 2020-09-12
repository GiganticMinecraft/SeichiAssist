package com.github.unchama.generic.effect.concurrent

import cats.Monad
import cats.effect.{CancelToken, Concurrent, Fiber, Sync}

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

  import AsymmetricTryableFiber._

  /**
   * Obtains the current status of the `Fiber`
   */
  def getCurrentStatus[G[_] : Sync]: G[FiberStatus[A]]

  /**
   * A computation that cancels the fiber if it is still running,
   * returning whether the cancellation happened or not.
   */
  def cancelIfRunning: F[Boolean]

  import cats.implicits._

  def isRunning[G[_] : Sync]: G[Boolean] = getCurrentStatus[G] map {
    case Running => true
    case _ => false
  }

  def isNotRunning[G[_] : Sync]: G[Boolean] = isRunning map (!_)

  def isComplete[G[_] : Sync]: G[Boolean] = getCurrentStatus[G] map {
    case Completed(_) => true
    case _ => false
  }

  def isIncomplete[G[_] : Sync]: G[Boolean] = isComplete map (!_)
}

object AsymmetricTryableFiber {

  sealed trait FiberStatus[+A]

  case object Running extends FiberStatus[Nothing]

  sealed trait FiberResult[+A] extends FiberStatus[A]

  case class Completed[+A](a: A) extends FiberResult[A]

  case object Cancelled extends FiberResult[Nothing]

  /**
   * Start concurrent execution of the source suspended in `F`.
   *
   * @return a [[AsymmetricTryableFiber]] which can be used to cancel, join or tryJoin the result
   */
  def start[F[_], A](fa: F[A])(implicit F: Concurrent[F]): F[AsymmetricTryableFiber[F, A]] = {
    import cats.implicits._

    for {
      completionPromise <- AsymmetricTryableDeferred.concurrent[F, FiberResult[A]]
      fiber <- F.start {
        fa >>= { a =>
          completionPromise.complete(Completed(a)).as(a)
        }
      }
    } yield new AsymmetricTryableFiber[F, A] {
      override def cancelIfRunning: F[Boolean] = fiber.cancel >> {
        completionPromise.complete(Cancelled).as(true).recover {
          case _: IllegalStateException =>
            // When `complete` was impossible
            false
          // otherwise rethrow
        }
      }

      override def getCurrentStatus[G[_] : Sync]: G[FiberStatus[A]] = {
        completionPromise.tryGet[G] map {
          case Some(value) => value
          case None => Running
        }
      }

      override def cancel: CancelToken[F] = cancelIfRunning.as(())

      override def join: F[A] = fiber.join
    }
  }

  /**
   * Creates a trivial value of [[AsymmetricTryableFiber]] which is always complete.
   */
  def completed[F[_], A](a: A)(implicit F: Monad[F]): AsymmetricTryableFiber[F, A] = {
    new AsymmetricTryableFiber[F, A] {
      override def getCurrentStatus[G[_] : Sync]: G[FiberStatus[A]] = Sync[G].pure(Completed(a))

      override def cancelIfRunning: F[Boolean] = F.pure(false)

      override def cancel: CancelToken[F] = F.unit

      override def join: F[A] = F.pure(a)
    }
  }

  /**
   * Creates a trivial value of [[AsymmetricTryableFiber]] which is always complete.
   */
  def unit[F[_]](implicit F: Monad[F]): AsymmetricTryableFiber[F, Unit] = completed[F, Unit](())
}
