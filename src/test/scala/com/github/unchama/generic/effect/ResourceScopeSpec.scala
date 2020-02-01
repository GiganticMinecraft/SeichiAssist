package com.github.unchama.generic.effect

import cats.Monad
import cats.effect.{IO, Resource}
import com.github.unchama.generic.effect.ResourceScope.SingleResourceScope
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ResourceScopeSpec extends AnyWordSpec with Matchers with MockFactory {

  case class NumberedObject(id: Int)

  "Default implementation of ResourceScope" should {
    val firstResourceScope: ResourceScope[IO, NumberedObject] = ResourceScope.unsafeCreate
    val secondResourceScope: ResourceScope[IO, NumberedObject] = ResourceScope.unsafeCreate

    def trackedResource(scope: ResourceScope[IO, NumberedObject],
                        obj: NumberedObject,
                        impureFinalizer: NumberedObject => Unit = _ => ()): Resource[IO, NumberedObject] = {
      val resource = Resource.make(IO.pure(obj))(o => IO { impureFinalizer(o) })

      scope.tracked(resource)
    }

    "recognize acquisition precisely in tracked scopes" in {
      firstResourceScope.isTracked(NumberedObject(0)).unsafeRunSync() mustBe false

      trackedResource(firstResourceScope, NumberedObject(0)).use { _ =>
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

      trackedResource(firstResourceScope, NumberedObject(0))
        .use { o => IO { resourceEffect(o) } }
        .unsafeRunSync()
    }

    "call finalizer of the resource exactly once on release" in {
      val finalizer = mockFunction[NumberedObject, Unit]

      finalizer.expects(NumberedObject(0)).once()

      trackedResource(firstResourceScope, NumberedObject(0), finalizer)
        .use { _ => IO.unit }
        .unsafeRunSync()
    }
  }
}
