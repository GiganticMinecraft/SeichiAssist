package com.github.unchama.generic.effect.concurrent

import cats.effect.IO
import cats.effect.std.Dispatcher
import cats.effect.unsafe.implicits.global
import com.github.unchama.testutil.concurrent.tests.ConcurrentEffectTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class AsymmetricSignallingRefSpec
    extends AnyWordSpec
    with ScalaCheckPropertyChecks
    with Matchers
    with ConcurrentEffectTest {

  import scala.concurrent.duration._

  type Value = Int

  "AsymmetricSignallingRef" should {
    import cats.implicits._

    "signal all the changes" in {
      val initialValue: Value = 0

      forAll(minSuccessful(10000)) { (updates: List[Value]) =>
        val task = Dispatcher.sequential[IO].use { implicit dispatcher =>
          for {
            ref <- AsymmetricSignallingRef.in[IO, IO, IO, Value](initialValue)
            updateResult <-
              ref.valuesAwait.use { stream =>
                for {
                  resultFiber <- stream.take(updates.length).compile.toList.start
                  _ <- updates.traverse(ref.set)
                  result <- resultFiber.joinWithNever
                } yield result
              }
          } yield updateResult
        }

        assertResult(updates)(task.timeout(1.second).unsafeRunSync())
      }
    }
  }
}
