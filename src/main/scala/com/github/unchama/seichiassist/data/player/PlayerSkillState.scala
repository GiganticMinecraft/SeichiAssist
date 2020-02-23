package com.github.unchama.seichiassist.data.player

import com.github.unchama.seichiassist.activeskill.effect.ActiveSkillEffect
import com.github.unchama.seichiassist.activeskill.{ActiveSkill, AssaultSkill, BreakArea, BreakSide, SeichiSkill}

case class PlayerSkillEffectState(obtainedEffects: Set[ActiveSkillEffect],
                                  selection: Option[ActiveSkillEffect])

case class PlayerSkillState(obtainedSkills: Set[SeichiSkill],
                            activeSkillBreakSide: Option[BreakSide], // TODO should be in configuration
                            isActiveSkillAvailable: Boolean,
                            activeSkill: Option[ActiveSkill],
                            assaultSkill: Option[AssaultSkill]) {
  def consumedActiveSkillPoint(): Int =
    obtainedSkills.map(_.requiredActiveSkillPoint).sum

  val activeSkillArea: Option[BreakArea] =
    activeSkill.map(new BreakArea(_, activeSkillBreakSide))

  val assaultSkillArea: Option[BreakArea] =
    assaultSkill.map(new BreakArea(_, None))
}
