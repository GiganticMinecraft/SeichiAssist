package com.github.unchama.seichiassist.subsystems.vote

import com.github.unchama.seichiassist.subsystems.vote.domain.{
  ChainVoteDayNumber,
  PlayerName,
  VotePoint
}

import java.util.UUID

trait VoteWriteAPI[F[_]] {

  /**
   * 投票ポイントをインクリメントする作用
   */
  def incrementVotePoint(playerName: PlayerName): F[Unit]

  /**
   * 連続投票を更新する作用
   */
  def updateChainVote(playerName: PlayerName): F[Unit]

}

object VoteWriteAPI {

  def apply[F[_]](implicit voteWriteAPI: VoteWriteAPI[F]): VoteWriteAPI[F] = implicitly

}

trait VoteReadAPI[F[_]] {

  /**
   * 投票ポイントを返す作用
   */
  def votePoint(uuid: UUID): F[VotePoint]

  /**
   * 連続投票日数を返す作用
   */
  def chainVoteDayNumber(uuid: UUID): F[ChainVoteDayNumber]

}

object VoteReadAPI {

  def apply[F[_]](implicit voteReadAPI: VoteReadAPI[F]): VoteReadAPI[F] = implicitly

}

trait VoteAPI[F[_]] extends VoteReadAPI[F] with VoteWriteAPI[F]
