package com.github.unchama.seichiassist.subsystems.donate

import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillPremiumEffect
import com.github.unchama.seichiassist.subsystems.donate.domain.{
  DonatePremiumEffectPoint,
  PremiumEffectPurchaseData
}

import java.util.UUID

trait DonateWriteAPI[F[_]] {

  /**
   * プレミアムエフェクトポイントを使用する作用
   */
  def useDonatePremiumEffectPoint(
    uuid: UUID,
    activeSkillPremiumEffect: ActiveSkillPremiumEffect
  ): F[Unit]

}

object DonateWriteAPI {

  def apply[F[_]](implicit ev: DonateWriteAPI[F]): DonateWriteAPI[F] = ev

}

trait DonateReadAPI[F[_]] {

  /**
   * 現在のプレミアムエフェクトポイントの合計を取得する作用
   */
  def currentPremiumEffectPoints(uuid: UUID): F[DonatePremiumEffectPoint]

  /**
   * プレミアムエフェクトの購入履歴を取得する作用
   */
  def donatePremiumEffectPointPurchaseHistory(uuid: UUID): F[Vector[PremiumEffectPurchaseData]]

  /**
   * プレミアムエフェクトの使用履歴を取得する作用
   */
  def donatePremiumEffectPointUsageHistory(uuid: UUID): F[Vector[PremiumEffectPurchaseData]]

}

object DonateReadAPI {

  def apply[F[_]](implicit ev: DonateReadAPI[F]): DonateReadAPI[F] = ev

}

trait DonateAPI[F[_]] extends DonateWriteAPI[F] with DonateReadAPI[F]
