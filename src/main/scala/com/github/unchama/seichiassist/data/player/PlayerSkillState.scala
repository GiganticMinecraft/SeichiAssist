package com.github.unchama.seichiassist.data.player

import com.github.unchama.seichiassist.activeskill.effect.ActiveSkillEffect
import com.github.unchama.seichiassist.activeskill.{ActiveSkill, AssaultSkill, BreakArea, BreakSide, SeichiSkill}
import com.github.unchama.seichiassist.data.player.PlayerSkillState.{PlayerActiveSkillState, PlayerAssaultSkillState, PlayerSkillEffectState}

class PlayerSkillState {
  var obtainedSkills: Set[SeichiSkill] = Set()

  def consumedActiveSkillPoint(): Int =
    obtainedSkills.map(_.requiredActiveSkillPoint).sum

  val effectState = new PlayerSkillEffectState()
  val activeSkillState = new PlayerActiveSkillState()
  val assaultSkillState = new PlayerAssaultSkillState()
}

object PlayerSkillState {
  class PlayerSkillEffectState {
    var obtainedEffects: Set[ActiveSkillEffect] = Set()

    var effectSelection: Option[ActiveSkillEffect] = None
  }

  class PlayerActiveSkillState {
    var activeSkillSelection: Option[ActiveSkill] = None

    var breakSideSelection: Option[BreakSide] = None

    var isSkillAvailable: Boolean = true

    def activeSkillArea(): Option[BreakArea] = activeSkillSelection.map(new BreakArea(_, breakSideSelection))
  }

  class PlayerAssaultSkillState {
    var assaultSkillSelection: Option[AssaultSkill] = None

    def assaultSkillArea(): Option[BreakArea] = assaultSkillSelection.map(new BreakArea(_, None))
  }
}