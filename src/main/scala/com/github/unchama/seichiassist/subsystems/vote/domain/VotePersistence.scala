package com.github.unchama.seichiassist.subsystems.vote.domain

import java.util.UUID

trait VotePersistence[F[_]] {

  /**
   * 投票回数をインクリメントする作用
   */
  def voteCounterIncrement(playerName: PlayerName): F[Unit]

  /**
   * 投票回数を返す作用
   */
  def voteCounter(uuid: UUID): F[VoteCounter]

  /**
   * 連続投票回数を更新する作用
   */
  def updateChainVote(playerName: PlayerName): F[Unit]

  /**
   * 連続投票日数を返す作用
   */
  def chainVoteDays(uuid: UUID): F[ChainVoteDayNumber]

  /**
   * effectPointを10増加させる作用
   */
  def increaseEffectPointsByTen(uuid: UUID): F[Unit]

  /**
   * effectPointを返す作用
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
