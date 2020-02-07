package com.github.unchama.generic.effect

import cats.effect.concurrent.Deferred
import cats.effect.{CancelToken, ContextShift, IO, Timer}
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext

class ConcurrentExtraSpec extends AnyWordSpec with Matchers with MockFactory {
  "withSelfCancellationToken" should {
    implicit val shift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

    "not interrupt execution of non-cancelling action" in {
      ConcurrentExtra.withSelfCancellation[IO, Int](_ => IO.pure(42)).unsafeRunSync() mustBe 42
    }

    "receive the cancellation token of the own computation" in {
      val subProcessFinalizer = mockFunction[Unit]
      val finalizer = mockFunction[Unit]

      inSequence {
        subProcessFinalizer.expects().once()
        finalizer.expects().once()
      }

      import scala.concurrent.duration._

      val program = for {
        promise <- Deferred[IO, CancelToken[IO]]
        _ <- ConcurrentExtra.withSelfCancellation[IO, Unit] { cancelToken =>
          for {
            _ <- promise.complete(cancelToken)
            _ <- IO.never.guarantee(IO(subProcessFinalizer()))
          } yield ()
        }.start
        returnedCancelToken <- promise.get
        _ <- IO.sleep(1.second) // let started fiber reach IO.never
        _ <- returnedCancelToken // subProcessFinalizer should be called
        _ <- IO(finalizer())
      } yield ()

      program.unsafeRunSync()
    }
  }
}
