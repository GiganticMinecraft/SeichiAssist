package com.github.unchama.seichiassist.seichiskill

import org.scalatest.wordspec.AnyWordSpec

class SeichiSkillSpec extends AnyWordSpec {
  "entryName" should {
    "be unique" in {
      val skillIds = SeichiSkill.values.map(_.entryName)
      skillIds.length == skillIds.toSet.size
    }
  }
}
