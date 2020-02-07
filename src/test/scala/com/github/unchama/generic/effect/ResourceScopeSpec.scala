package com.github.unchama.generic.effect

import cats.effect.{ContextShift, IO, Resource, Timer}
import com.github.unchama.generic.effect.ResourceScope.SingleResourceScope
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext

class ResourceScopeSpec extends AnyWordSpec with Matchers with MockFactory {

  case class NumberedObject(id: Int)

  "Default implementation of ResourceScope" should {
    implicit val shift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

    val firstResourceScope: ResourceScope[IO, NumberedObject] = ResourceScope.unsafeCreate
    val secondResourceScope: ResourceScope[IO, NumberedObject] = ResourceScope.unsafeCreate

    def useTracked[A](scope: ResourceScope[IO, NumberedObject],
                      obj: NumberedObject,
                      impureFinalizer: NumberedObject => Unit = _ => ())
                     (use: NumberedObject => IO[A]): IO[A] = {
      val resource = Resource.make(IO.pure(obj))(o => IO(impureFinalizer(o)))

      scope.useTracked(resource)(use)
    }

    "recognize acquisition precisely in tracked scopes" in {
      firstResourceScope.isTracked(NumberedObject(0)).unsafeRunSync() mustBe false

      useTracked(firstResourceScope, NumberedObject(0)) { _ =>
        IO {
          firstResourceScope.isTracked(NumberedObject(0)).unsafeRunSync() mustBe true

          firstResourceScope.isTracked(NumberedObject(1)).unsafeRunSync() mustBe false

          secondResourceScope.isTracked(NumberedObject(0)).unsafeRunSync() mustBe false
        }
      }.unsafeRunSync()

      firstResourceScope.isTracked(NumberedObject(0)).unsafeRunSync() mustBe false
    }

    "not interrupt the usage of the resource" in {
      val resourceEffect = mockFunction[NumberedObject, Unit]

      resourceEffect.expects(NumberedObject(0)).once()

      useTracked(firstResourceScope, NumberedObject(0)) { o => IO { resourceEffect(o) } }.unsafeRunSync()
    }

    "call finalizer of the resource exactly once on release" in {
      val finalizer = mockFunction[NumberedObject, Unit]

      finalizer.expects(NumberedObject(0)).once()

      useTracked(firstResourceScope, NumberedObject(0), finalizer) { _ => IO.unit }
        .unsafeRunSync()
    }

    "be coherent with external cancellation by explicit release" in {
      val impureFunction = mockFunction[NumberedObject, Unit]
      val finalizer = mockFunction[NumberedObject, Unit]
      val impureFunction2 = mockFunction[Unit, Unit]

      inSequence {
        impureFunction.expects(NumberedObject(0)).once()
        finalizer.expects(NumberedObject(0)).once()
        impureFunction2.expects(()).once()
      }

      import cats.implicits._
      import scala.concurrent.duration._

      val program = for {
        _ <-
          useTracked(firstResourceScope, NumberedObject(0), finalizer) { o =>
            IO { impureFunction(o) } >> IO.never
          }.start
        _ <- IO.sleep(1.second) >> firstResourceScope.release(NumberedObject(0))
        _ <- IO(impureFunction2(()))
      } yield ()

      program.unsafeRunSync()
    }

    "be coherent with external cancellation by releaseAll" in {
      val impureFunction = mockFunction[NumberedObject, Unit]
      val finalizer = mockFunction[NumberedObject, Unit]
      val impureFunction2 = mockFunction[Unit, Unit]

      inSequence {
        impureFunction.expects(NumberedObject(0)).once()
        finalizer.expects(NumberedObject(0)).once()
        impureFunction2.expects(()).once()
      }

      import cats.implicits._
      import scala.concurrent.duration._

      val program = for {
        _ <-
          useTracked(firstResourceScope, NumberedObject(0), finalizer) { o =>
            IO { impureFunction(o) } >> IO.never
          }.start
        _ <- IO.sleep(1.second) >> firstResourceScope.releaseAll
        _ <- IO(impureFunction2(()))
      } yield ()

      program.unsafeRunSync()
    }
  }

  "Singleton resource scope" should {
    implicit val shift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

    val firstResourceScope: SingleResourceScope[IO, NumberedObject] = ResourceScope.unsafeCreateSingletonScope
    val secondResourceScope: SingleResourceScope[IO, NumberedObject] = ResourceScope.unsafeCreateSingletonScope

    def useTrackedForSome[A](scope: SingleResourceScope[IO, NumberedObject],
                             obj: NumberedObject,
                             impureFinalizer: NumberedObject => Unit = _ => ())
                            (use: NumberedObject => IO[A]): IO[Option[A]] = {
      val resource = Resource.make(IO.pure(obj))(o => IO { impureFinalizer(o) })

      scope.useTrackedForSome(resource)(use)
    }

    "recognize acquisition precisely in tracked scopes" in {
      firstResourceScope.isTrackedUnlifted(NumberedObject(0)).unsafeRunSync() mustBe false

      useTrackedForSome(firstResourceScope, NumberedObject(0)) { _ =>
        IO {
          firstResourceScope.isTrackedUnlifted(NumberedObject(0)).unsafeRunSync() mustBe true

          firstResourceScope.isTrackedUnlifted(NumberedObject(1)).unsafeRunSync() mustBe false

          secondResourceScope.isTrackedUnlifted(NumberedObject(0)).unsafeRunSync() mustBe false
        }
      }.unsafeRunSync()

      firstResourceScope.isTrackedUnlifted(NumberedObject(0)).unsafeRunSync() mustBe false
    }

    "not interrupt the usage of the resource" in {
      val resourceEffect = mockFunction[NumberedObject, Unit]

      resourceEffect.expects(NumberedObject(0)).once()

      useTrackedForSome(firstResourceScope, NumberedObject(0)) { o => IO { resourceEffect(o) } }
        .unsafeRunSync()
    }

    "call finalizer of the resource exactly once on release" in {
      val finalizer = mockFunction[NumberedObject, Unit]

      finalizer.expects(NumberedObject(0)).once()

      useTrackedForSome(firstResourceScope, NumberedObject(0), finalizer) { _ => IO.unit }
        .unsafeRunSync()
    }

    "not acquire more than one resource" in {
      val impureFunction = mockFunction[NumberedObject, Unit]
      val finalizer = mockFunction[NumberedObject, Unit]

      finalizer.expects(NumberedObject(0)).once()
      finalizer.expects(NumberedObject(1)).never()
      impureFunction.expects(NumberedObject(0)).once()
      impureFunction.expects(NumberedObject(1)).never()

      useTrackedForSome(firstResourceScope, NumberedObject(0), finalizer) { o0 =>
        for {
          _ <- IO { impureFunction(o0) }
          _ <- useTrackedForSome(firstResourceScope, NumberedObject(1), finalizer) { o1 =>
            IO { impureFunction(o1) }
          }
        } yield ()
      }.unsafeRunSync()
    }

    "be coherent with external cancellation by explicit release" in {
      val impureFunction = mockFunction[NumberedObject, Unit]
      val finalizer = mockFunction[NumberedObject, Unit]
      val impureFunction2 = mockFunction[Unit, Unit]

      inSequence {
        impureFunction.expects(NumberedObject(0)).once()
        finalizer.expects(NumberedObject(0)).once()
        impureFunction2.expects(()).once()
      }

      import cats.implicits._
      import scala.concurrent.duration._

      val program = for {
        _ <-
          useTrackedForSome(firstResourceScope, NumberedObject(0), finalizer) { o =>
            IO { impureFunction(o) } >> IO.never
          }.start
        _ <- IO.sleep(1.second) >> firstResourceScope.releaseSome(NumberedObject(0))
        _ <- IO(impureFunction2(()))
      } yield ()

      program.unsafeRunSync()
    }

    "be coherent with external cancellation by releaseAll" in {
      val impureFunction = mockFunction[NumberedObject, Unit]
      val finalizer = mockFunction[NumberedObject, Unit]
      val impureFunction2 = mockFunction[Unit, Unit]

      inSequence {
        impureFunction.expects(NumberedObject(0)).once()
        finalizer.expects(NumberedObject(0)).once()
        impureFunction2.expects(()).once()
      }

      import cats.implicits._
      import scala.concurrent.duration._

      val program = for {
        _ <-
          useTrackedForSome(firstResourceScope, NumberedObject(0), finalizer) { o =>
            IO { impureFunction(o) } >> IO.never
          }.start
        _ <- IO.sleep(1.second) >> firstResourceScope.releaseAll.value
        _ <- IO(impureFunction2(()))
      } yield ()

      program.unsafeRunSync()
    }
  }
}
