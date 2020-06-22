package com.github.unchama.seichiassist.seichiskill.effect

import org.scalatest.wordspec.AnyWordSpec

class ActiveSkillEffectSpec extends AnyWordSpec {
  "entryName" should {
    "be unique" in {
      val effectIds = UnlockableActiveSkillEffect.values.map(_.entryName)
      effectIds.length == effectIds.toSet.size
    }
  }
}
