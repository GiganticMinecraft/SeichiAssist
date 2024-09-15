package com.github.unchama.seichiassist.subsystems.breakskilltriggerconfig.domain

import enumeratum.{Enum, EnumEntry}

/**
 * トリガ条件の集合を表現する。
 * これらの値は整地スキルでブロックを破壊する際に、
 * プレイヤーの設定に応じて使われる。
 */
sealed trait BreakSkillTriggerConfigKey extends EnumEntry

object BreakSkillTriggerConfigKey extends Enum[BreakSkillTriggerConfigKey] {

  val values: IndexedSeq[BreakSkillTriggerConfigKey] = findValues

  /**
   * マナを消費しきった場合にブロックを破壊するかどうか
   */
  case object ManaFullyConsumed extends BreakSkillTriggerConfigKey

}
