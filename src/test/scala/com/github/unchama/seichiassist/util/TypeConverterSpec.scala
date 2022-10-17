package com.github.unchama.seichiassist.util

import org.scalatest.wordspec.AnyWordSpec

class TypeConverterSpec extends AnyWordSpec {
  "toSecond" should {
    "convert ticks to second" in {
      assert(40 == TypeConverter.toSecond(800))
    }
  }

  "toTimeString" should {
    "convert second to time string" in {
      assert("16分" == TypeConverter.toTimeString(987))
    }
  }
}
