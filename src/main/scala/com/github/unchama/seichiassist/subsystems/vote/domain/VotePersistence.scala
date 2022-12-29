package com.github.unchama.seichiassist.subsystems.vote.domain

import java.util.UUID

trait VotePersistence[F[_]] {

  /**
   * プレイヤーデータを作成する作用
   */
  def createPlayerData(uuid: UUID): F[Unit]

  /**
   * 投票回数をインクリメントする作用
   */
  def incrementVoteCount(playerName: PlayerName): F[Unit]

  /**
   * 投票回数を返す作用
   */
  def currentVoteCount(uuid: UUID): F[VoteCounter]

  /**
   * 連続投票回数を更新する作用
   */
  def updateConsecutiveVoteStreak(playerName: PlayerName): F[Unit]

  /**
   * 連続投票日数を返す作用
   */
  def currentConsecutiveVoteStreakDay(uuid: UUID): F[ChainVoteDayNumber]

  /**
   * [[EffectPoint]]を指定分だけ増加させる作用
   */
  def increaseEffectPoints(uuid: UUID, effectPoint: EffectPoint): F[Unit]

  /**
   * [[EffectPoint]]を減少させる作用
   */
  def decreaseEffectPoints(uuid: UUID, effectPoint: EffectPoint): F[Unit]

  /**
   * 指定プレイヤーの[[EffectPoint]]を返す作用
   */
  def effectPoints(uuid: UUID): F[EffectPoint]

  /**
   * 投票特典を受け取った回数を増加させる作用
   */
  def increaseVoteBenefits(uuid: UUID, amount: VoteBenefit): F[Unit]

  /**
   * 投票特典を受け取った回数を返す作用
   */
  def receivedVoteBenefits(uuid: UUID): F[VoteBenefit]

}
