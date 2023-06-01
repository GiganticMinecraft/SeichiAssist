package com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain

import enumeratum.{Enum, EnumEntry}

/**
 * 定義されたブロックを破壊するかしないかを表すためのフラグの名前を集めたEnum
 */
sealed trait BreakSkillTargetConfigKey extends EnumEntry

object BreakSkillTargetConfigKey extends Enum[BreakSkillTargetConfigKey] {

  val values: IndexedSeq[BreakSkillTargetConfigKey] = findValues

  /**
   * Chestブロックを破壊するかを示すフラグ名
   */
  case object Chest extends BreakSkillTargetConfigKey

  /**
   * ネザークォーツ系ブロックを破壊するかを示すフラグ名
   */
  case object NetherQuartz extends BreakSkillTargetConfigKey

}
