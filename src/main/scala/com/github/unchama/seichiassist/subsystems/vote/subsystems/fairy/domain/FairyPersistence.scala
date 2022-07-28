package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.{
  AppleAmount,
  AppleAteByFairyRank,
  AppleAteByFairyRankTopFour,
  AppleOpenState,
  FairyEndTime,
  FairyRecoveryMana,
  FairySummonCost,
  FairyUsingState
}

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

  /**
   * 妖精の効果が終了する時刻を変更する
   */
  def updateFairyEndTime(uuid: UUID, fairyEndTime: FairyEndTime): F[Unit]

  /**
   * 妖精の効果が終了する時刻を取得する
   */
  def fairyEndTime(uuid: UUID): F[Option[FairyEndTime]]

  /**
   * 妖精が食べたりんごの量を増加させる
   */
  def increaseAppleAteByFairy(uuid: UUID, appleAmount: AppleAmount): F[Unit]

  /**
   * 妖精が食べたりんごの量を取得する
   */
  def appleAteByFairy(uuid: UUID): F[AppleAmount]

  /**
   * 自分の妖精に食べさせたりんごの量の順位を返す
   */
  def appleAteByFairyMyRanking(uuid: UUID): F[AppleAteByFairyRank]

  /**
   * 妖精に食べさせたりんごの量の順位上位4件を返す
   */
  def appleAteByFairyRankingTopFour(uuid: UUID): F[AppleAteByFairyRankTopFour]

}
