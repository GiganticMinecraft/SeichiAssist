package com.github.unchama.generic.algebra.typeclass

import cats.Order
import com.github.unchama.generic.algebra.typeclasses.HasSuccessor
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class HasSuccessorSpec extends AnyWordSpec with ScalaCheckPropertyChecks with Matchers {

  import cats.implicits._

  type T = Int

  implicit val intArbitrary: Arbitrary[T] = Arbitrary(Gen.choose(-10000, 10000))

  implicit val testTInstance: HasSuccessor[T] = new HasSuccessor[T] {
    override val order: Order[T] = implicitly

    override def successor(x: T): Option[T] = Some(x + 1)
  }

  "HasSuccessor.closedRange" should {
    "be empty when lower is strictly larger than upper" in {
      forAll { (lower: T, upper: T) =>
        whenever(lower > upper) {
          assert(testTInstance.closedRange(lower, upper).isEmpty)
        }
      }
    }

    "be sequence of successive elements starting from lower" in {
      forAll { (lower: T, upper: T) =>
        whenever(lower <= upper) {
          val range = testTInstance.closedRange(lower, upper)

          assert(range.head == lower)
          assert {
            range.sliding(2).forall {
              case Seq(a, b) => a + 1 == b
              case _         => true
            }
          }
        }
      }
    }

    "be sequence of elements between lower and upper inclusively" in {
      forAll { (lower: T, upper: T) =>
        assert {
          testTInstance.closedRange(lower, upper).forall { a => lower <= a && a <= upper }
        }
      }
    }
  }
}
