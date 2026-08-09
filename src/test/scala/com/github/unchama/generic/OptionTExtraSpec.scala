package com.github.unchama.generic

import cats.data.OptionT
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.{Applicative, Id}
import org.scalatest.wordspec.AnyWordSpec

class OptionTExtraSpec extends AnyWordSpec {
  "failIf" should {
    "produce failing [a] =>> OptionT[Id, a] on true" in {
      assert(OptionTExtra.failIf[Id](failCondition = true) == OptionT.none(Applicative[Id]))
    }

    "produce failing [a] =>> OptionT[IO, a] on true" in {
      assert(OptionTExtra.failIf[IO](failCondition = true).value.unsafeRunSync().isEmpty)
    }

    "produce succeeding [a] =>> OptionT[Id, a] on false" in {
      assert(
        OptionTExtra.failIf[Id](failCondition = false) == OptionT.some(())(Applicative[Id])
      )
    }

    "produce succeeding [a] =>> OptionT[IO, a] on false" in {
      assert(OptionTExtra.failIf[IO](failCondition = false).value.unsafeRunSync().nonEmpty)
    }
  }
}
