package com.github.unchama.testutil.concurrent.sequencer

import java.util.concurrent.ConcurrentLinkedQueue

import cats.effect.{ContextShift, IO}
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext
import scala.util.Random

class SequencerSpec extends AnyWordSpec {
  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val shift: ContextShift[IO] = IO.contextShift(ec)

  val sequencerImplementations: List[Sequencer[IO]] = List(
    LinkedSequencer[IO]
  )

  val randomizedProgramListSize = 20000

  "all implementations" should {
    "sequence randomized executions" in {
      sequencerImplementations.foreach { sequencer =>
        info(sequencer.toString)

        import scala.jdk.CollectionConverters._
        import cats.implicits._

        val queue = new ConcurrentLinkedQueue[Int]()

        val program = for {
          blockerList <- sequencer.newBlockerList
          programList =
          blockerList
            .take(randomizedProgramListSize * 2)
            .toList
            .grouped(2)
            .zipWithIndex
            .map { case (adjacentBlockers, index) =>
              val List(pre, post) = adjacentBlockers

              pre.await() >> IO(queue.add(index)) >> post.await()
            }
            .toList
          scrambledPrograms = Random.shuffle(programList)
          startedFibers <- scrambledPrograms.map(_.start).sequence
          _ <- startedFibers.map(_.join).sequence
        } yield ()

        program.unsafeRunSync()

        val completionOrder = queue.asScala.toList

        assert(completionOrder == completionOrder.sorted)
        assert(completionOrder.size == randomizedProgramListSize)
      }
    }
  }
}
