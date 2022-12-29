package com.github.unchama.seichiassist.subsystems.donate

import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillPremiumEffect
import com.github.unchama.seichiassist.subsystems.donate.domain.{
  DonatePremiumEffectPoint,
  PremiumEffectPurchaseData
}

import java.util.UUID

trait DonatePremiumPointWriteAPI[F[_]] {

  /**
   * @return プレミアムエフェクトポイントを使用する作用
   */
  def useDonatePremiumEffectPoint(
    uuid: UUID,
    activeSkillPremiumEffect: ActiveSkillPremiumEffect
  ): F[Unit]

}

object DonatePremiumPointWriteAPI {

  def apply[F[_]](implicit ev: DonatePremiumPointWriteAPI[F]): DonatePremiumPointWriteAPI[F] =
    ev

}

trait DonatePremiumPointReadAPI[F[_]] {

  /**
   * @return 現在のプレミアムエフェクトポイントの合計を取得する作用
   */
  def currentPoint(uuid: UUID): F[DonatePremiumEffectPoint]

  /**
   * @return プレミアムエフェクトの購入履歴を取得する作用
   */
  def fetchGrantHistory(uuid: UUID): F[Vector[PremiumEffectPurchaseData]]

  /**
   * @return プレミアムエフェクトの使用履歴を取得する作用
   */
  def fetchUseHistory(uuid: UUID): F[Vector[PremiumEffectPurchaseData]]

}

object DonatePremiumPointReadAPI {

  def apply[F[_]](implicit ev: DonatePremiumPointReadAPI[F]): DonatePremiumPointReadAPI[F] = ev

}

trait DonatePremiumPointAPI[F[_]]
    extends DonatePremiumPointWriteAPI[F]
    with DonatePremiumPointReadAPI[F]
