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
   * 妖精が召喚するためのコストを変更する
   */
  def updateFairySummonCost(uuid: UUID, fairySummonCost: FairySummonCost): F[Unit]

  /**
   * 妖精を召喚するためのコストを取得する
   */
  def fairySummonCost(uuid: UUID): F[FairySummonCost]

  /**
   * 妖精が召喚されているかを更新する
   */
  def updateFairyUsingState(uuid: UUID, fairyUsingState: FairyUsingState): F[Unit]

  /**
   * 妖精が召喚されているかを取得する
   */
  def fairyUsingState(uuid: UUID): F[FairyUsingState]

  /**
   * 妖精が回復するマナの量を変更する
   */
  def updateFairyRecoveryMana(uuid: UUID, fairyRecoveryMana: FairyRecoveryMana): F[Unit]

  /**
   * 妖精が回復するマナの量を取得する
   */
  def fairyRecoveryMana(uuid: UUID): F[FairyRecoveryMana]

}
