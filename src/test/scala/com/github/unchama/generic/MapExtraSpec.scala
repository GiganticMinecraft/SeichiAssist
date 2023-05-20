package com.github.unchama.generic

import org.scalatest.wordspec.AnyWordSpec

class MapExtraSpec extends AnyWordSpec {
  "fillOnBaseSet" should {
    "return empty Map" in {
      assert(MapExtra.fillOnBaseSet[String, Int](Map(), Set(), 1).isEmpty)
    }

    "fail if map's key is not sub-set of set" in {
      assertThrows[IllegalArgumentException] {
        MapExtra.fillOnBaseSet[String, Int](Map("a" -> 2), Set(), 1)
      }
    }

    "return filled Map" in {
      assert(
        MapExtra.fillOnBaseSet(
          Map("a" -> 1, "b" -> 2, "c" -> 3),
          Set("a", "b", "c", "d"),
          4
        ) == Map("a" -> 1, "b" -> 2, "c" -> 3, "d" -> 4)
      )
    }

    "return filled Map when given Map is empty" in {
      assert(
        MapExtra.fillOnBaseSet(Map(), Set("A", "B", "C", "D"), 42) == Map(
          "A" -> 42,
          "B" -> 42,
          "C" -> 42,
          "D" -> 42
        )
      )
    }

  }
}
