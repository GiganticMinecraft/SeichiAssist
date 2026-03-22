package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

import scala.util.Random

/**
 * 妖精の1回の回復サイクルあたりのマナ回復量。
 *
 * この値は妖精の召喚時に一度だけ決定され、妖精が活動中は常にこの値を基準にマナ回復計算が行われる。
 * 実際の回復量はがちゃりんごの在庫状況・ボーナスロール・ドラゲナイタイムによって変動する。
 *
 * @see [[com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyBaseRecoveryMana]]
 *      召喚時の回復量決定ロジック
 */
case class FairyBaseRecoveryMana(amount: Int) {
  require(amount >= 0)
}

object FairyBaseRecoveryMana {
  def manaAmountAt(levelCappedManaAmount: Double): FairyBaseRecoveryMana = {
    require(levelCappedManaAmount >= 0.0, "levelCappedManaAmountは非負の値で指定してください。")
    FairyBaseRecoveryMana(
      (levelCappedManaAmount / 10 - levelCappedManaAmount / 30 + new Random()
        .nextInt((levelCappedManaAmount / 20).toInt) / 2.9).toInt + 200
    )
  }
}
