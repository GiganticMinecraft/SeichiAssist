package com.github.unchama.seichiassist.subsystems.donate.domain

import java.util.UUID

trait DonatePersistence[F[_]] {

  /**
   * DonatePremiumEffectPointを増加させる作用
   */
  def addDonatePremiumEffectPoint(
    playerName: PlayerName,
    donatePremiumEffectPoint: DonatePremiumEffectPoint
  ): F[Unit]

  /**
   * 現在のプレミアムエフェクトポイントの合計を取得する作用
   */
  def currentPremiumEffectPoints(uuid: UUID): F[DonatePremiumEffectPoint]

}
