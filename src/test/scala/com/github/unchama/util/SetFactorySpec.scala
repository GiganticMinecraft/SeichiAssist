package com.github.unchama.util

import com.github.unchama.util.collection.SetFactory
import org.scalatest.wordspec.AnyWordSpec

class SetFactorySpec extends AnyWordSpec {
  "SetFactory" should {
    "yield empty set when no parameter is given" in {
      assert(new java.util.HashSet[String]() == SetFactory.of())
    }

    "yield set containing exactly the given parameters" in {
      val expected = new java.util.HashSet[String]()
      expected.add("value1")
      expected.add("value2")

      assert(expected == SetFactory.of("value1", "value2"))
    }
  }
}
