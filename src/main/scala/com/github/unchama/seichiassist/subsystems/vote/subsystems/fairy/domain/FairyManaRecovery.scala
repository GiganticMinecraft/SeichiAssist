package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  FairyManaRecoveryState,
  FairyBaseRecoveryMana
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
   * @param consumedGachaAppleCount
   *   実際に消費するがちゃりんごの個数。
   *   MineStack の在庫が不足している場合は在庫量に切り捨てられる。
   * @param manaBeforeDragonNightMultiplier
   *   りんご消費比率を適用した後、ドラゲナイ乗算前の回復量。
   * @param finalRecoveredMana
   *   ドラゲナイタイム乗算を適用した最終的な回復マナ量。
   * @param state
   *   回復結果の分類
   */
  case class ManaRecoveryResult(
    consumedGachaAppleCount: Int,
    manaBeforeDragonNightMultiplier: Double,
    finalRecoveredMana: Double,
    state: FairyManaRecoveryState
  )

  /**
   * マナ回復量と回復状態を計算する。
   *
   * - 回復量は、通常のがちゃりんごの回復量の70%を基本とする
   * - 3%の確率で、がちゃりんご1個あたり0.3マナのボーナス回復が発生する
   * - ドラゲナイタイムのときはマナ回復量が2倍になる
   * - 回復量ががちゃりんご1個分(300)に満たない場合は、がちゃりんごを消費せずに回復する
   *
   * @param recoveryMana
   *   妖精の回復マナ設定値
   * @param mineStackedAmount
   *   プレイヤーの MineStack に存在している、がちゃりんごの在庫数
   * @param bonusRoll
   *   呼び出し元が生成した `[0.0, 1.0)` の一様乱数
   *   `<= 0.03` のときボーナス回復が発動する
   * @param isDragonNight
   *   ドラゲナイタイム中であるかどうか。`true` のとき回復量が 2 倍になる
   * @return
   *   計算結果
   */
  def compute(
    recoveryMana: FairyBaseRecoveryMana,
    mineStackedAmount: Long,
    bonusRoll: Double,
    isDragonNight: Boolean
  ): ManaRecoveryResult = {
    val manaPerApple = 300
    val baseManaRecoveryRatio = 0.7

    val bonusRollThreshold = 0.03
    val bonusManaPerApple = 0.3

    val dragonNightMultiplier = 2.0

    val pureAppleConsumeAmount = recoveryMana.amount / manaPerApple
    val isLessThanSingleAppleRecovery =
      recoveryMana.amount > 0 && recoveryMana.amount < manaPerApple
    val consumedGachaAppleCount =
      if (isLessThanSingleAppleRecovery) 0
      else Math.min(pureAppleConsumeAmount, mineStackedAmount).toInt
    val baseAmount = recoveryMana.amount * baseManaRecoveryRatio
    val bonusAmount =
      if (bonusRoll <= bonusRollThreshold) consumedGachaAppleCount * bonusManaPerApple else 0.0

    val manaBeforeDragonNightMultiplier =
      if (isLessThanSingleAppleRecovery) baseAmount
      else if (pureAppleConsumeAmount == 0) 0.0
      else (baseAmount * (consumedGachaAppleCount.toDouble / pureAppleConsumeAmount)) + bonusAmount

    val multiplier = if (isDragonNight) dragonNightMultiplier else 1.0
    val finalRecoveredMana = manaBeforeDragonNightMultiplier * multiplier

    val state =
      if (isLessThanSingleAppleRecovery)
        FairyManaRecoveryState.RecoverWithoutAppleButLessThanAApple
      else if (manaBeforeDragonNightMultiplier == 0.0)
        FairyManaRecoveryState.RecoveredWithoutApple
      else
        FairyManaRecoveryState.RecoveredWithApple

    ManaRecoveryResult(
      consumedGachaAppleCount,
      manaBeforeDragonNightMultiplier,
      finalRecoveredMana,
      state
    )
  }

}
