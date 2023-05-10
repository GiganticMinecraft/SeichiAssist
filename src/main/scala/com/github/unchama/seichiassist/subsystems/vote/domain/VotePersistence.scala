package com.github.unchama.seichiassist.subsystems.vote.domain

import java.util.UUID

trait VotePersistence[F[_]] {

  /**
   * プレイヤーの投票データを初期化します。この作用はプレイヤーがサーバーに参加するたびに呼ばれるため、作用が冪等でなければなりません。
   *
   * @return プレイヤーデータを作成する作用
   */
  def createPlayerData(uuid: UUID): F[Unit]

  /**
   * @return 投票回数をインクリメントする作用
   */
  def incrementVoteCount(uuid: UUID): F[Unit]

  /**
   * @return 投票回数を返す作用
   */
  def currentVoteCount(uuid: UUID): F[VoteCount]

  /**
   * @return 連続投票回数を更新する作用
   */
  def updateConsecutiveVoteStreak(uuid: UUID): F[Unit]

  /**
   * @return 連続投票日数を返す作用
   */
  def currentConsecutiveVoteStreakDay(uuid: UUID): F[ChainVoteDayNumber]

  /**
   * @return [[EffectPoint]]を指定分だけ増加させる作用
   */
  def increaseEffectPoints(uuid: UUID, effectPoint: EffectPoint): F[Unit]

  /**
   * @return [[EffectPoint]]を減少させる作用
   */
  def decreaseEffectPoints(uuid: UUID, effectPoint: EffectPoint): F[Unit]

  /**
   * @return 指定プレイヤーの[[EffectPoint]]を返す作用
   */
  def effectPoints(uuid: UUID): F[EffectPoint]

  /**
   * @return 投票特典を受け取った回数を増加させる作用
   */
  def increaseVoteBenefits(uuid: UUID, amount: VoteBenefit): F[Unit]

  /**
   * @return 投票特典を受け取った回数を返す作用
   */
  def receivedVoteBenefits(uuid: UUID): F[VoteBenefit]

}
