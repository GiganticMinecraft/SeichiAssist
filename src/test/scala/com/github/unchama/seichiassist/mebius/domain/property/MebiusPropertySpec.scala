package com.github.unchama.seichiassist.mebius.domain.property

import java.util.UUID

import org.scalatest.wordspec.AnyWordSpec

class MebiusPropertySpec extends AnyWordSpec {
  "initial mebius property" should {
    "be valid" in {
      val testPlayerName = "testPlayer"
      val testPlayerUuid = UUID.randomUUID().toString

      // exception thrown if invalid
      MebiusProperty.initialProperty(testPlayerName, testPlayerUuid) equals
        MebiusProperty.initialProperty(testPlayerName, testPlayerUuid)
    }
  }
}
