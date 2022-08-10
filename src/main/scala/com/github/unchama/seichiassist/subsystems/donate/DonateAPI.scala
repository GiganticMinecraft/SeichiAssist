package com.github.unchama.seichiassist.subsystems.donate

import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillPremiumEffect

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

trait DonateAPI[F[_]] extends DonateWriteAPI[F]
