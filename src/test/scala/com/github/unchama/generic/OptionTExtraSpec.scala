package com.github.unchama.generic

import cats.data.OptionT
import cats.effect.IO
import cats.{Applicative, Id}
import org.scalatest.wordspec.AnyWordSpec

class OptionTExtraSpec extends AnyWordSpec {
  "failIf" should {
    "produce failing OptionT[Id, *] on true" in {
      assert(OptionTExtra.failIf[Id](failCondition = true) == OptionT.none(Applicative[Id]))
    }

    "produce failing OptionT[IO, *] on true" in {
      assert(OptionTExtra.failIf[IO](failCondition = true).value.unsafeRunSync().isEmpty)
    }

    "produce succeeding OptionT[Id, *] on false" in {
      assert(
        OptionTExtra.failIf[Id](failCondition = false) == OptionT.some(())(Applicative[Id])
      )
    }

    "produce succeeding OptionT[IO, *] on false" in {
      assert(OptionTExtra.failIf[IO](failCondition = false).value.unsafeRunSync().nonEmpty)
    }
  }
}
