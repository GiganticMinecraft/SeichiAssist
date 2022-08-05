package com.github.unchama.seichiassist.subsystems.donate.domain

trait DonatePersistence[F[_]] {

  /**
   * DonatePremiumEffectPointを増加させる作用
   */
  def addDonatePremiumEffectPoint(
    playerName: PlayerName,
    donatePremiumEffectPoint: DonatePremiumEffectPoint
  ): F[Unit]

  /**
   * プレミアムエフェクト購入履歴を読み込む
   */
  def loadPremiumEffectPurchaseHistory: F[Vector[PremiumEffectPurchaseData]]

}
