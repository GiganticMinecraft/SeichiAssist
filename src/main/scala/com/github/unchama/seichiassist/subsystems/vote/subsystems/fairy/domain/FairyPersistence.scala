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
   * 妖精が有効な時間の状態
   */
  def updateFairyValidTimeState(uuid: UUID, validTimeState: FairyValidTimeState): F[Unit]

  /**
   * 妖精が有効な時間の状態を取得します
   */
  def fairySummonState(uuid: UUID): F[FairyValidTimeState]

  /**
   * 妖精が召喚されているかを更新します
   */
  def updateFairyUsingState(uuid: UUID, fairyUsingState: FairyUsingState): F[Unit]

  /**
   * 妖精が召喚されているかを取得します
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
