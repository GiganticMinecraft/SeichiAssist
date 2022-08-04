package com.github.unchama.seichiassist.subsystems.donate

import com.github.unchama.seichiassist.subsystems.donate.domain.{
  DonatePremiumEffectPoint,
  PlayerName
}

trait DonateWriteAPI[F[_]] {

  /**
   * DonatePremiumEffectPointを増加させる作用
   */
  def addDonatePremiumEffectPoint(
    playerName: PlayerName,
    donatePremiumEffectPoint: DonatePremiumEffectPoint
  ): F[Unit]

}

object DonateWriteAPI {

  def apply[F[_]](implicit ev: DonateWriteAPI[F]): DonateWriteAPI[F] = ev

}

trait DonateAPI[F[_]] extends DonateWriteAPI[F]
