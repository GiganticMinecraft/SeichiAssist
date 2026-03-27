package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

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

  /**
   * 妖精召喚時の基本マナ回復量を計算する。
   *
   * 結果は以下の3つの成分の和として求まる:
   *
   *   1. 確定成分: `levelCappedManaAmount / 10 - levelCappedManaAmount / 30`
   *      レベルキャップ後マナ量から一定の割合で決まる値。
   *
   *   2. ランダム成分: `floor(levelCappedManaAmount / 20) * randomRoll / 2.9`
   *      マナ量に応じた幅の中でランダムにばらつく値。
   *      `randomRoll` が 0.0 に近いほど小さく、1.0 に近いほど大きくなる。
   *
   *   3. 最低保証値: 200
   *      マナ量によらず常に加算される下限値。
   *
   * @param levelCappedManaAmount レベルキャップ適用後のマナ量（非負）
   * @param randomRoll            呼び出し元が生成した [0.0, 1.0) の一様乱数
   */
  def manaAmountAt(levelCappedManaAmount: Double, randomRoll: Double): FairyBaseRecoveryMana = {
    require(levelCappedManaAmount >= 0.0, "levelCappedManaAmountは非負の値で指定してください。")
    require(randomRoll >= 0.0 && randomRoll < 1.0, "randomRollは [0.0, 1.0) の範囲で指定してください。")
    val maxJitterSteps = (levelCappedManaAmount / 20).toInt
    val jitter = (maxJitterSteps * randomRoll) / 2.9
    FairyBaseRecoveryMana(
      (levelCappedManaAmount / 10 - levelCappedManaAmount / 30 + jitter).toInt + 200
    )
  }
}
