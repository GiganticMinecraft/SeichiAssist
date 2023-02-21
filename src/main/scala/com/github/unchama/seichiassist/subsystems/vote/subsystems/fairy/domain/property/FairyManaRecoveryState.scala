package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

sealed trait FairyManaRecoveryState

/**
 * 妖精がマナを回復する際に考えられるステータスを定義したオブジェクト
 */
object FairyManaRecoveryState {

  /**
   * マナが完全に回復している
   */
  case object Full extends FairyManaRecoveryState

  /**
   * マナを回復したがりんごを消費しなかった
   */
  case object RecoveredWithoutApple extends FairyManaRecoveryState

  /**
   * りんごを消費してマナを回復した
   */
  case object RecoveredWithApple extends FairyManaRecoveryState

}
