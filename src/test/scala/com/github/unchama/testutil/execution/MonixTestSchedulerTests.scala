package com.github.unchama.testutil.execution

import monix.eval.Task
import monix.execution.schedulers.TestScheduler
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration.{Duration, FiniteDuration}

trait MonixTestSchedulerTests extends ScalaFutures {

  def awaitForProgram[U](program: Task[U], tickDuration: FiniteDuration = Duration.Zero)
                        (implicit testScheduler: TestScheduler): U = {
    val future = program.runToFuture

    testScheduler.tick(tickDuration)

    future.futureValue
  }

}
