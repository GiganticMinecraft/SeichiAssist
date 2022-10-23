package com.github.unchama.seichiassist.subsystems.vote

import com.github.unchama.seichiassist.subsystems.vote.domain._

import java.util.UUID

trait VoteWriteAPI[F[_]] {

  /**
   * effectPointを減少させる作用
   */
  def decreaseEffectPoint(uuid: UUID, effectPoint: EffectPoint): F[Unit]

  /**
   * effectPointを増加させる作用
   */
  def increaseEffectPointsByTen(uuid: UUID): F[Unit]

  /**
   * 投票特典を受け取った回数を増加させる作用
   */
  def increaseVoteBenefits(uuid: UUID, benefit: VoteBenefit): F[Unit]

}

object VoteWriteAPI {

  def apply[F[_]](implicit ev: VoteWriteAPI[F]): VoteWriteAPI[F] = ev

}

trait VoteReadAPI[F[_], Player] {

  /**
   * 投票回数を返す作用
   */
  def voteCounter(uuid: UUID): F[VoteCounter]

  /**
   * 連続投票日数を返す作用
   */
  def chainVoteDayNumber(uuid: UUID): F[ChainVoteDayNumber]

  /**
   * effectPointを返す作用
   */
  def effectPoints(player: Player): F[EffectPoint]

  /**
   * 投票特典を受け取った回数を返す作用
   */
  def receivedVoteBenefits(uuid: UUID): F[VoteBenefit]

  /**
   * 投票特典を受け取っていない回数を返す作用
   */
  def restVoteBenefits(uuid: UUID): F[VoteBenefit]

}

object VoteReadAPI {

  def apply[F[_], Player](implicit ev: VoteReadAPI[F, Player]): VoteReadAPI[F, Player] = ev

}

trait VoteAPI[F[_], Player] extends VoteReadAPI[F, Player] with VoteWriteAPI[F]
