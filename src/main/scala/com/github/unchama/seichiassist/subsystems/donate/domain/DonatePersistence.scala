package com.github.unchama.seichiassist.subsystems.donate.domain

import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillPremiumEffect

import java.util.UUID

trait DonatePersistence[F[_]] {

  /**
   * @return プレミアムエフェクトポイントを増加させる作用
   */
  def addDonatePremiumEffectPoint(
    playerName: PlayerName,
    obtainedPremiumEffectPoint: Obtained
  ): F[Unit]

  /**
   * @return プレミアムエフェクトポイントを使用する作用
   */
  def useDonatePremiumEffectPoint(uuid: UUID, effect: ActiveSkillPremiumEffect): F[Unit]

  /**
   * @return 現在のプレミアムエフェクトポイントの合計を取得する作用
   */
  def currentPremiumEffectPoints(uuid: UUID): F[DonatePremiumEffectPoint]

  /**
   * @return プレミアムエフェクトの購入履歴を取得する作用
   */
  def donatePremiumEffectPointPurchaseHistory(uuid: UUID): F[Vector[PremiumEffectPurchaseData]]

  /**
   * @return プレミアムエフェクトの使用履歴を取得する作用
   */
  def donatePremiumEffectPointUsageHistory(uuid: UUID): F[Vector[PremiumEffectPurchaseData]]

}
