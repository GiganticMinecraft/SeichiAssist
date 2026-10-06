package com.github.unchama.generic.effect.stream

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class StreamExtraSpec extends AnyWordSpec with ScalaCheckPropertyChecks with Matchers {

  "StreamExtra.takeEvery" should {
    "be equivalent to accessing every n elements" in {
      implicit val positiveIntGenerator: Arbitrary[Int] =
        Arbitrary(Gen.chooseNum(1, Int.MaxValue))

      val n = 3

      forAll { (vector: Vector[Int]) =>
        val expected = (vector.indices by n).map(vector.apply)
        val result = StreamExtra.takeEvery(n)(fs2.Stream(vector: _*)).compile.toList

        assertResult(expected)(result)
      }
    }
  }

}
