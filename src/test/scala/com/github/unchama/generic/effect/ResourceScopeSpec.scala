package com.github.unchama.generic.effect

import cats.effect.{ContextShift, IO, Resource, Timer}
import com.github.unchama.generic.effect.ResourceScope.SingleResourceScope
import com.github.unchama.testutil.concurrent.sequencer.LinkedSequencer
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext

class ResourceScopeSpec extends AnyWordSpec with Matchers with MockFactory {

  case class NumberedObject(id: Int)

  "Default implementation of ResourceScope" should {
    implicit val shift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

    val firstResourceScope: ResourceScope[IO, IO, NumberedObject] = ResourceScope.unsafeCreate
    val secondResourceScope: ResourceScope[IO, IO, NumberedObject] = ResourceScope.unsafeCreate

    def useTracked[A](scope: ResourceScope[IO, IO, NumberedObject],
                      obj: NumberedObject,
                      impureFinalizer: NumberedObject => Unit = _ => ())
                     (use: NumberedObject => IO[A]): IO[A] = {
      val resource = Resource.make(IO.pure(obj))(o => IO(impureFinalizer(o)))

      scope.useTracked(resource)(use)
    }

    "recognize acquisition precisely in tracked scopes" in {
      firstResourceScope.isTracked(NumberedObject(0)).unsafeRunSync() mustBe false

      useTracked(firstResourceScope, NumberedObject(0)) { _ =>
        for {
          _ <- IO { firstResourceScope.trackedHandlers.unsafeRunSync() mustBe Set(NumberedObject(0)) }
          _ <- IO { secondResourceScope.trackedHandlers.unsafeRunSync() mustBe Set() }
          _ <- useTracked(firstResourceScope, NumberedObject(1)) { _ =>
            for {
              _ <- IO {
                firstResourceScope.trackedHandlers.unsafeRunSync() mustBe Set(NumberedObject(0), NumberedObject(1))
              }
              _ <- IO {
                secondResourceScope.trackedHandlers.unsafeRunSync() mustBe Set()
              }
            } yield ()
          }
          _ <- IO { firstResourceScope.trackedHandlers.unsafeRunSync() mustBe Set(NumberedObject(0)) }
        } yield ()
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

      def runImpureFunction(o: NumberedObject) = IO(impureFunction(o))

      val runImpureFunction2 = IO(impureFunction2())

      val program = for {
        blockerList <- LinkedSequencer[IO].newBlockerList
        _ <-
          useTracked(firstResourceScope, NumberedObject(0), finalizer) { o =>
            //noinspection ZeroIndexToHead
            runImpureFunction(o) >>
              blockerList(0).await() >>
              IO.never
          }.start
        _ <- blockerList(1).await()
        releaseAction <- firstResourceScope.getReleaseAction(NumberedObject(0))
        _ <- releaseAction
        _ <- runImpureFunction2
      } yield ()

      program.unsafeRunSync()

      firstResourceScope.isTracked(NumberedObject(0)).unsafeRunSync() mustBe false
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

      def runImpureFunction(o: NumberedObject): IO[Unit] = IO(impureFunction(o))

      val runImpureFunction2 = IO(impureFunction2())

      val program = for {
        blockerList <- LinkedSequencer[IO].newBlockerList
        _ <-
          useTracked(firstResourceScope, NumberedObject(0), finalizer) { o =>
            //noinspection ZeroIndexToHead
            runImpureFunction(o) >>
              blockerList(0).await >>
              IO.never
          }.start
        _ <- blockerList(1).await()
        releaseAction <- firstResourceScope.getReleaseAllAction
        _ <- releaseAction
        _ <- runImpureFunction2
      } yield ()

      program.unsafeRunSync()

      firstResourceScope.isTracked(NumberedObject(0)).unsafeRunSync() mustBe false
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
      firstResourceScope.isTracked(NumberedObject(0)).unsafeRunSync() mustBe false

      useTrackedForSome(firstResourceScope, NumberedObject(0)) { _ =>
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

      def runImpureFunction(o: NumberedObject): IO[Unit] = IO(impureFunction(o))

      useTrackedForSome(firstResourceScope, NumberedObject(0), finalizer) { o0 =>
        for {
          _ <- runImpureFunction(o0)
          _ <- useTrackedForSome(firstResourceScope, NumberedObject(1), finalizer) { o1 =>
            runImpureFunction(o1)
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

      def runImpureFunction(o: NumberedObject): IO[Unit] = IO(impureFunction(o))

      val runImpureFunction2 = IO(impureFunction2())

      val program = for {
        blockerList <- LinkedSequencer[IO].newBlockerList
        _ <-
          useTrackedForSome(firstResourceScope, NumberedObject(0), finalizer) { o =>
            //noinspection ZeroIndexToHead
            runImpureFunction(o) >>
              blockerList(0).await >>
              IO.never
          }.start
        _ <- blockerList(1).await()
        releaseAction <- firstResourceScope.getReleaseAction(NumberedObject(0))
        _ <- releaseAction.value
        _ <- runImpureFunction2
      } yield ()

      program.unsafeRunSync()

      firstResourceScope.isTracked(NumberedObject(0)).unsafeRunSync() mustBe false
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

      def runImpureFunction(o: NumberedObject): IO[Unit] = IO(impureFunction(o))

      val runImpureFunction2 = IO(impureFunction2())

      val program = for {
        blockerList <- LinkedSequencer[IO].newBlockerList
        _ <-
          useTrackedForSome(firstResourceScope, NumberedObject(0), finalizer) { o =>
            //noinspection ZeroIndexToHead
            runImpureFunction(o) >>
              blockerList(0).await() >>
              IO.never
          }.start
        _ <- blockerList(1).await()
        releaseAction <- firstResourceScope.getReleaseAllAction
        _ <- releaseAction.value
        _ <- runImpureFunction2
      } yield ()

      program.unsafeRunSync()

      firstResourceScope.isTracked(NumberedObject(0)).unsafeRunSync() mustBe false
    }
  }
}
