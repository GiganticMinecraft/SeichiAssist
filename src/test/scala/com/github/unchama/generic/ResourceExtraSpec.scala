package com.github.unchama.generic

import cats.data.OptionT
import cats.effect.{IO, Resource}
import com.github.unchama.generic.ResourceExtra.unwrapOptionTResource
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ResourceExtraSpec extends AnyWordSpec with Matchers {
  import cats.implicits._

  "unwrapOptionTResource" should {
    val pureValue = 42

    def someFunction(x: Int): Int = x * x

    {
      val unwrappedSomeResource: Resource[IO, Option[Int]] =
        unwrapOptionTResource(Resource.liftF(OptionT.some[IO](pureValue)))

      "yield succeeding resource for the resource lifted from OptionT.some" in {
        unwrappedSomeResource.use(IO.pure).unsafeRunSync().contains(pureValue)
      }

      "yield resource causing effect on use" in {
        unwrappedSomeResource
          .use(_.map(x => IO.pure(someFunction(x))).sequence)
          .unsafeRunSync() mustBe Some(someFunction(pureValue))
      }
    }

    {
      val unwrappedEmptyResource: Resource[IO, Option[Int]] =
        unwrapOptionTResource(Resource.liftF(OptionT.none[IO, Int]))

      "yield failing resource for the resource lifted from OptionT.none" in {
        unwrappedEmptyResource.use(IO.pure).unsafeRunSync().isEmpty
      }

      "yield resource causing effect on use, even for resources failing to allocate" in {
        unwrappedEmptyResource.use(_ => IO.pure(pureValue)).unsafeRunSync() mustBe pureValue
      }
    }
  }
}
