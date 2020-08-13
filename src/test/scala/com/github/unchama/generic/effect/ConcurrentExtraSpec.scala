package com.github.unchama.generic.effect

import cats.effect.concurrent.Deferred
import cats.effect.{CancelToken, ContextShift, IO, Timer}
import com.github.unchama.testutil.concurrent.sequencer.LinkedSequencer
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

      val callSubProcessFinalizer = IO(subProcessFinalizer())
      val callFinalizer = IO(finalizer())

      import cats.implicits._

      val program = for {
        blockerList <- LinkedSequencer[IO].newBlockerList
        promise <- Deferred[IO, CancelToken[IO]]
        _ <- ConcurrentExtra.withSelfCancellation[IO, Unit] { cancelToken =>
          for {
            _ <- promise.complete(cancelToken)
            //noinspection ZeroIndexToHead
            _ <- {
              blockerList(0).await() >> IO.never
            }.guarantee {
              callSubProcessFinalizer
            }
          } yield ()
        }.start
        returnedCancelToken <- promise.get
        _ <- blockerList(1).await() // let started fiber reach IO.never
        _ <- returnedCancelToken // subProcessFinalizer should be called
        _ <- callFinalizer
      } yield ()

      program.unsafeRunSync()
    }
  }
}
