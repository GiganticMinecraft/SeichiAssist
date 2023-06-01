package com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain

import enumeratum.{Enum, EnumEntry}

/**
 * 予め決められたブロックの集合を表現する。
 * これらの値は整地スキルでブロックを破壊する際に、
 * プレイヤーの設定に応じて一部のブロックを除外する際に使われる。
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
  case object MadeFromNetherQuartz extends BreakSkillTargetConfigKey

}
