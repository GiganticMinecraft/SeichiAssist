package com.github.unchama.seichiassist.seichiskill

import enumeratum.values.{IntEnum, IntEnumEntry}

/**
 * プレーヤーがスキルを使用するかどうか、
 * 使用するなら下側または上側のどちらを破壊するかの設定状態を表す列挙型。
 *
 * デュアルブレイクとトリアルブレイクに限り「下側破壊」の概念が存在する。
 */
sealed abstract class ActiveSkillUsageMode(val value: Int) extends IntEnumEntry

object ActiveSkillUsageMode extends IntEnum[ActiveSkillUsageMode] {
  case object Disabled extends ActiveSkillUsageMode(0)
  case object Active extends ActiveSkillUsageMode(1)
  case object LowerActive extends ActiveSkillUsageMode(2)

  /**
   * 選択されたスキルに応じて、設定項目のトグル先を計算する。
   *
   * 選択されたスキルが「デュアル・ブレイク」または「トリアル・ブレイク」である場合は
   * Disabled => Active => LowerActive => Disabled
   * の順で巡回し、それ以外ではDisabled => Active => Disabledのように巡回する。
   */
  def toggle(selectedSkill: ActiveSkill)(usageIntention: ActiveSkillUsageMode): ActiveSkillUsageMode =
    usageIntention match {
      case Disabled => Active
      case Active => selectedSkill match {
        case SeichiSkill.DualBreak | SeichiSkill.TrialBreak =>
          LowerActive
        case _ =>
          Disabled
      }
      case LowerActive => Disabled
    }

  val values: IndexedSeq[ActiveSkillUsageMode] = findValues
}
