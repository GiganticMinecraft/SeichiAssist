package com.github.unchama.seichiassist.data.player

import com.github.unchama.seichiassist.seichiskill._
import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillEffect
import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillEffect.NoEffect

case class PlayerSkillEffectState(obtainedEffects: Set[ActiveSkillEffect],
                                  selection: ActiveSkillEffect)

object PlayerSkillEffectState {
  val initial: PlayerSkillEffectState =
    PlayerSkillEffectState(Set(), NoEffect)
}

case class PlayerSkillState(obtainedSkills: Set[SeichiSkill],
                            activeSkillBreakSide: Option[BreakSide], // TODO should be in configuration
                            isActiveSkillAvailable: Boolean,
                            activeSkill: Option[ActiveSkill],
                            assaultSkill: Option[AssaultSkill]) {
  lazy val consumedActiveSkillPoint: Int =
    obtainedSkills.map(_.requiredActiveSkillPoint).sum

  /**
   * `skill` が [[obtainedSkills]] に追加された状態を返す。
   */
  def obtained(skill: SeichiSkill): PlayerSkillState =
    this.copy(obtainedSkills = obtainedSkills + skill)

  /**
   * `skill` が取得されている状態の場合、それを選択した状態を、そうでなければこの状態を返す。
   */
  def select(skill: SeichiSkill): PlayerSkillState =
    if (obtainedSkills.contains(skill)) {
      skill match {
        case skill: ActiveSkill =>
          this.copy(activeSkill = Some(skill))
        case skill: AssaultSkill =>
          this.copy(assaultSkill = Some(skill))
      }
    } else this

  /**
   * `skill` の獲得に必要な前提スキルのうち、この状態において解除されていない最初のものを返す。
   */
  def lockedDependency(skill: SeichiSkill): Option[SeichiSkill] =
    SkillDependency.prerequisites(skill).find(p => !obtainedSkills.contains(p))

  /**
   * `skill`が選択されていた場合、その選択を解除した状態を、
   * そうでなければこの状態を返す。
   */
  def deselect(skill: SeichiSkill): PlayerSkillState =
    skill match {
      case skill: ActiveSkill =>
        if (activeSkill.contains(skill))
          this.copy(activeSkill = None)
        else
          this
      case skill: AssaultSkill =>
        if (assaultSkill.contains(skill))
          this.copy(assaultSkill = None)
        else
          this
    }

  def deselected(): PlayerSkillState = this.copy(activeSkill = None, assaultSkill = None)
}

object PlayerSkillState {
  val initial: PlayerSkillState =
    PlayerSkillState(
      obtainedSkills = Set(),
      activeSkillBreakSide = None,
      isActiveSkillAvailable = false,
      activeSkill = None,
      assaultSkill = None
    )
}
