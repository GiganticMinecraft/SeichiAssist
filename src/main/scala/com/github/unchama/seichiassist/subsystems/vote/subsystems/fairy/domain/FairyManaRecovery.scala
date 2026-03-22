package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  FairyManaRecoveryState,
  FairyRecoveryMana
}

/**
 * マナ妖精によるマナ回復量を純粋に計算するドメインオブジェクト。
 *
 * IO・Bukkit への依存を持たず、単体テストが可能。
 * 呼び出し元は乱数・現在時刻などの副作用を事前に解決し、結果を引数として渡す責務を持つ。
 */
object FairyManaRecovery {

  /**
   * マナ回復計算の結果。
   *
   * @param appleConsumed
   *   実際に消費するがちゃりんごの個数。
   *   MineStack の在庫が不足している場合は在庫量に切り捨てられる。
   * @param manaFromApples
   *   りんご消費比率を適用した後、ドラゲナイ乗算前の回復量。
   * @param finalAmount
   *   ドラゲナイタイム乗算を適用した最終的な回復マナ量。
   * @param state
   *   回復結果の分類。メッセージ選択などに使用する。
   */
  case class ManaRecoveryResult(
    appleConsumed: Int,
    manaFromApples: Double,
    finalAmount: Double,
    state: FairyManaRecoveryState
  )

  /**
   * マナ回復量と回復状態を計算する。
   *
   * @param recoveryMana
   *   妖精の回復マナ設定値。
   * @param mineStackedAmount
   *   プレイヤーの MineStack に積まれているがちゃりんごの現在個数。
   * @param bonusRoll
   *   呼び出し元が生成した `[0.0, 1.0)` の一様乱数。
   *   `<= 0.03` のときボーナス回復が発動する。
   * @param isDragonNight
   *   ドラゲナイタイム中であるかどうか。`true` のとき回復量が 2 倍になる。
   * @return
   *   計算結果を格納した [[ManaRecoveryResult]]。
   */
  def compute(
    recoveryMana: FairyRecoveryMana,
    mineStackedAmount: Long,
    bonusRoll: Double,
    isDragonNight: Boolean
  ): ManaRecoveryResult = {
    val pureAppleConsumeAmount = recoveryMana.recoveryMana / 300
    val appleConsumed = Math.min(pureAppleConsumeAmount, mineStackedAmount).toInt
    val baseAmount = recoveryMana.recoveryMana * 0.7
    val bonusAmount = if (bonusRoll <= 0.03) appleConsumed * 0.3 else 0.0
    val totalBase = baseAmount + bonusAmount

    val manaFromApples =
      if (pureAppleConsumeAmount == 0) 0.0
      else totalBase * (appleConsumed.toDouble / pureAppleConsumeAmount)

    val multiplier = if (isDragonNight) 2.0 else 1.0
    val finalAmount = manaFromApples * multiplier

    val state =
      if (totalBase == 0.0 && manaFromApples < 300.0)
        FairyManaRecoveryState.RecoverWithoutAppleButLessThanAApple
      else if (manaFromApples == 0.0)
        FairyManaRecoveryState.RecoveredWithoutApple
      else
        FairyManaRecoveryState.RecoveredWithApple

    ManaRecoveryResult(appleConsumed, manaFromApples, finalAmount, state)
  }

}
