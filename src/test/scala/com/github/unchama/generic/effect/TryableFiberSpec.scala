package com.github.unchama.generic.effect

import cats.effect.concurrent.Deferred
import cats.effect.{ContextShift, IO, Timer}
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext

class TryableFiberSpec extends AnyWordSpec {
  "Unit fiber" should {
    "be always complete" in {
      val fiber = TryableFiber.unit[IO]

      val assertionProgram =
        for {
          isComplete <- fiber.isComplete
          wasIncomplete <- fiber.cancelIfIncomplete
        } yield {
          assert(isComplete)
          assert(!wasIncomplete)
        }

      assertionProgram.unsafeRunSync()
    }
  }

  "Started fiber" should {
    implicit val shift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    implicit val tiemr: Timer[IO] = IO.timer(ExecutionContext.global)

    "never be complete for non-terminating IO" in {
      val assertionProgram =
        for {
          nonTerminatingFiber <- TryableFiber.start[IO, Nothing](IO.never)
          isComplete <- nonTerminatingFiber.isComplete
          wasIncomplete <- nonTerminatingFiber.cancelIfIncomplete
        } yield {
          assert(!isComplete)
          assert(wasIncomplete)
        }

      assertionProgram.unsafeRunSync()
    }

    "follow the completion status" in {
      val assertionProgram = {
        for {
          deferred <- Deferred[IO, Unit]
          sleepFiber <- TryableFiber.start(deferred.get)

          // the fiber should not be complete because deferred.get semantically blocks
          isCompleteFirst <- sleepFiber.isComplete

          _ <- deferred.complete(())
          _ <- sleepFiber.join

          // the fiber must be complete after it is joint
          isCompleteFinally <- sleepFiber.isComplete
        } yield {
          assert(!isCompleteFirst)
          assert(isCompleteFinally)
        }
      }

      assertionProgram.unsafeRunSync()
    }
  }
}
