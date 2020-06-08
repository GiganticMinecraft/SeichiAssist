package com.github.unchama.seichiassist.seichiskill

import com.github.unchama.seichiassist.seichiskill.SeichiSkill.{DualBreak, TrialBreak}
import enumeratum.values.{IntEnum, IntEnumEntry}

/**
 * プレーヤーがスキルを使用するかどうか、
 * 使用するなら下側または上側のどちらを破壊するかの設定状態を表す列挙型。
 *
 * デュアルブレイクとトリアルブレイクに限り「下側破壊」の概念が存在する。
 */
sealed abstract class SeichiSkillUsageMode(val value: Int) extends IntEnumEntry {
  /**
   * [[SeichiSkillUsageMode.toggle()]]を`skill`とこの値について計算する
   */
  final def nextMode(skill: SeichiSkill): SeichiSkillUsageMode =
    SeichiSkillUsageMode.toggle(skill)(this)

  /**
   * [[SeichiSkillUsageMode.modeString()]]を`skill`とこの値について計算する
   */
  final def modeString(skill: SeichiSkill): String =
    SeichiSkillUsageMode.modeString(skill)(this)
}

object SeichiSkillUsageMode extends IntEnum[SeichiSkillUsageMode] {
  case object Disabled extends SeichiSkillUsageMode(0)
  case object Active extends SeichiSkillUsageMode(1)
  case object LowerActive extends SeichiSkillUsageMode(2)

  /**
   * 選択されたスキルに応じて、設定項目のトグル先を計算する。
   *
   * 選択されたスキルが「デュアル・ブレイク」または「トリアル・ブレイク」である場合は
   * Disabled => Active => LowerActive => Disabled
   * の順で巡回し、それ以外ではDisabled => Active => Disabledのように巡回する。
   */
  def toggle(selectedSkill: SeichiSkill)(usageIntention: SeichiSkillUsageMode): SeichiSkillUsageMode =
    usageIntention match {
      case Disabled => Active
      case Active => selectedSkill match {
        case DualBreak | TrialBreak =>
          LowerActive
        case _ =>
          Disabled
      }
      case LowerActive => Disabled
    }

  def modeString(selectedSkill: SeichiSkill)(usageIntention: SeichiSkillUsageMode): String = {
    val dualOrTrialBreak = Seq(DualBreak, TrialBreak).contains(selectedSkill)

    usageIntention match {
      case Disabled => "OFF"
      case Active => if (dualOrTrialBreak) "ON-Above（上向き）" else "ON"
      case LowerActive => if (dualOrTrialBreak) "ON-Under（下向き）" else "ON"
    }
  }

  val values: IndexedSeq[SeichiSkillUsageMode] = findValues
}
