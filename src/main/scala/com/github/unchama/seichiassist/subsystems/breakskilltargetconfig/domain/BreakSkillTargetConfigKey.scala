package com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain

import enumeratum.{Enum, EnumEntry}

/**
 * 定義されたブロックを破壊するかしないかを表すためのフラグの名前を集めたEnum
 */
sealed trait BreakSkillTargetConfigKey extends EnumEntry

object BreakSkillTargetConfigKey extends Enum[BreakSkillTargetConfigKey] {

  val values: IndexedSeq[BreakSkillTargetConfigKey] = findValues

  /**
   * チェストを破壊するかどうか
   */
  case object Chest extends BreakSkillTargetConfigKey

  /**
   * ネザークォーツをクラフトしたブロックを破壊するかどうか
   */
  case object NetherQuartz extends BreakSkillTargetConfigKey

}
