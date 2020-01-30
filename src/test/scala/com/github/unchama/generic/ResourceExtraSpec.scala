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

    val unwrappedSomeResource: Resource[IO, Option[Int]] =
      unwrapOptionTResource(Resource.liftF(OptionT.some[IO](pureValue)))

    val unwrappedEmptyResource: Resource[IO, Option[Int]] =
      unwrapOptionTResource(Resource.liftF(OptionT.none[IO, Int]))

    "yield succeeding resource for the resource lifted from OptionT.some" in {
      unwrappedSomeResource.use(IO.pure).unsafeRunSync().contains(pureValue)
    }

    "yield failing resource for the resource lifted from OptionT.none" in {
      unwrappedEmptyResource.use(IO.pure).unsafeRunSync().isEmpty
    }

    "yield resource causing effect on use" in {
      unwrappedSomeResource.use(_.map(x => IO.pure(x - 1)).sequence).unsafeRunSync() mustBe Some(pureValue - 1)
    }

    "yield resource causing effect on use, even for resources failing to allocate" in {
      unwrappedEmptyResource.use(_ => IO.pure(pureValue)).unsafeRunSync() mustBe pureValue
    }
  }
}
