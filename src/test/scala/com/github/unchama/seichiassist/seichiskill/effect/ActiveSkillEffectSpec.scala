package com.github.unchama.seichiassist.seichiskill.effect

import org.scalatest.wordspec.AnyWordSpec

class ActiveSkillEffectSpec extends AnyWordSpec {
  "entryName" should {
    "be unique" in {
      val effectIds = (ActiveSkillNormalEffect.values ++ ActiveSkillPremiumEffect.values).map(_.entryName)
      effectIds.length == effectIds.toSet.size
    }
  }
}
