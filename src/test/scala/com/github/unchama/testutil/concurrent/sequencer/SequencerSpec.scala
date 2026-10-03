package com.github.unchama.testutil.concurrent.sequencer

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalatest.wordspec.AnyWordSpec

import java.util.concurrent.ConcurrentLinkedQueue
import scala.util.Random

class SequencerSpec extends AnyWordSpec {
  val sequencerImplementations: List[Sequencer[IO]] = List(LinkedSequencer[IO])

  val randomizedProgramListSize = 20000

  "all implementations" should {
    "sequence randomized executions" in {
      sequencerImplementations.foreach { sequencer =>
        info(sequencer.toString)

        import cats.implicits._

        import scala.jdk.CollectionConverters._

        val queue = new ConcurrentLinkedQueue[Int]()

        val program = for {
          blockerList <- sequencer.newBlockerList
          indexedPrograms = {
            blockerList.take(randomizedProgramListSize * 2).toList.grouped(2).zipWithIndex.map {
              case (adjacentBlockers, index) =>
                val List(pre, post) = adjacentBlockers

                pre.await() >> IO(queue.add(index)) >> post.await()
            }
          }
          scrambledPrograms = Random.shuffle(indexedPrograms).toList
          startedFibers <- scrambledPrograms.traverse(_.start)
          _ <- startedFibers.traverse(_.joinWithNever)
        } yield ()

        program.unsafeRunSync()

        val completionOrder = queue.asScala.toList

        assert(completionOrder == completionOrder.sorted)
        assert(completionOrder.size == randomizedProgramListSize)
      }
    }
  }
}
