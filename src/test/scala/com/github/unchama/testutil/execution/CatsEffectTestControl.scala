package com.github.unchama.testutil.execution

import cats.effect.IO
import cats.effect.testkit.TestControl
import cats.effect.unsafe.implicits.global

import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration}

trait CatsEffectTestControl {

  /**
   * Runs a program with a virtual-time deadline.
   *
   * A one-nanosecond grace period lets work scheduled exactly at the deadline finish, matching
   * the boundary behavior of the virtual scheduler previously used by these tests.
   */
  def awaitForProgram[U](program: IO[U], tickDuration: FiniteDuration = Duration.Zero): U = {
    val boundedProgram =
      if (tickDuration == Duration.Zero) program
      else
        program.timeoutTo(
          tickDuration + 1.nanosecond,
          IO.raiseError(
            new AssertionError(
              s"The controlled program did not complete within $tickDuration of virtual time"
            )
          )
        )

    TestControl.executeEmbed(boundedProgram).unsafeRunSync()
  }
}

object CatsEffectTestControl extends CatsEffectTestControl
