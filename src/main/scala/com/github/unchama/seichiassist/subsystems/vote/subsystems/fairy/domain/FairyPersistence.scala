package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

import java.util.UUID

trait FairyPersistence[F[_]] {

  /**
   * 妖精に開放するりんごの状態を変更する
   */
  def changeAppleOpenState(uuid: UUID, openState: AppleOpenState): F[Unit]

  /**
   * 妖精に開放するりんごの状態を取得する
   */
  def appleOpenState(uuid: UUID): F[AppleOpenState]

  /**
   * 妖精を召喚する状態を変更します。
   */
  def updateFairySummonState(uuid: UUID, fairySummonCost: FairySummonState): F[Unit]

  /**
   * 妖精を召喚する状態を取得します
   */
  def fairySummonState(uuid: UUID): F[FairySummonState]

}
