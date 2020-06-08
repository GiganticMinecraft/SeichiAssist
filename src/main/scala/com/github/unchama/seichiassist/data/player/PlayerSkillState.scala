package com.github.unchama.seichiassist.data.player

import com.github.unchama.seichiassist.seichiskill.SeichiSkillUsageMode.Disabled
import com.github.unchama.seichiassist.seichiskill._
import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillEffect.NoEffect
import com.github.unchama.seichiassist.seichiskill.effect.{ActiveSkillEffect, UnlockableActiveSkillEffect}

case class PlayerSkillEffectState(obtainedEffects: Set[UnlockableActiveSkillEffect],
                                  selection: ActiveSkillEffect)

object PlayerSkillEffectState {
  val initial: PlayerSkillEffectState =
    PlayerSkillEffectState(Set(), NoEffect)
}

case class PlayerSkillState(obtainedSkills: Set[SeichiSkill],
                            usageMode: SeichiSkillUsageMode,
                            activeSkill: Option[ActiveSkill],
                            assaultSkill: Option[AssaultSkill]) {
  lazy val consumedActiveSkillPoint: Int =
    obtainedSkills.toList.map(_.requiredActiveSkillPoint).sum

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
      usageMode = Disabled,
      activeSkill = None,
      assaultSkill = None
    )

  def fromUnsafeConfiguration(obtainedSkills: Set[SeichiSkill],
                              usageMode: SeichiSkillUsageMode,
                              activeSkill: Option[ActiveSkill],
                              assaultSkill: Option[AssaultSkill]): PlayerSkillState = {
    def notObtained(skill: SeichiSkill): Boolean = !obtainedSkills.contains(skill)

    val selections = Seq(activeSkill, assaultSkill).flatten
    if (selections.exists(notObtained))
      initial
    else
      PlayerSkillState(obtainedSkills, usageMode, activeSkill, assaultSkill)
  }
}
