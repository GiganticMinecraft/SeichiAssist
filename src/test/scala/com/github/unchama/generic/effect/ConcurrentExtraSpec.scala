package com.github.unchama.generic.effect

import cats.effect.{Deferred, IO}
import cats.effect.unsafe.implicits.global
import com.github.unchama.testutil.concurrent.sequencer.LinkedSequencer
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ConcurrentExtraSpec extends AnyWordSpec with Matchers with MockFactory {
  "withSelfCancellationToken" should {
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

      val runSubProcessFinalizer = IO(subProcessFinalizer())
      val runFinalizer = IO(finalizer())

      import cats.implicits._

      val program = for {
        blockerList <- LinkedSequencer[IO].newBlockerList
        promise <- Deferred[IO, IO[Unit]]
        _ <- ConcurrentExtra
          .withSelfCancellation[IO, Unit] { cancelToken =>
            for {
              _ <- promise.complete(cancelToken)
              // noinspection ZeroIndexToHead
              _ <- {
                blockerList(0).await() >> IO.never
              }.guarantee {
                runSubProcessFinalizer
              }
            } yield ()
          }
          .start
        cancellationAction <- promise.get
        _ <- blockerList(1).await() // let started fiber reach IO.never
        _ <- cancellationAction // subProcessFinalizer should be called
        _ <- runFinalizer
      } yield ()

      program.unsafeRunSync()
    }
  }
}
