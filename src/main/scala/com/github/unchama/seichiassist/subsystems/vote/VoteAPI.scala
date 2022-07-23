package com.github.unchama.seichiassist.subsystems.vote

import com.github.unchama.seichiassist.subsystems.vote.domain.VotePoint

import java.util.UUID

trait VoteWriteAPI[F[_]] {

  /**
   * 投票ポイントをインクリメントする作用
   */
  def incrementVotePoint(uuid: UUID): F[Unit]

  /**
   * 連続投票を更新する作用
   */
  def updateChainVote(uuid: UUID): F[Unit]

}

object VoteWriteAPI {

  def apply[F[_]](implicit voteWriteAPI: VoteWriteAPI[F]): VoteWriteAPI[F] = implicitly

}

trait VoteReadAPI[F[_]] {

  /**
   * 投票ポイントを返す作用
   */
  def votePoint(uuid: UUID): VotePoint

}

object VoteReadAPI {

  def apply[F[_]](implicit voteReadAPI: VoteReadAPI[F]): VoteReadAPI[F] = implicitly

}

trait VoteAPI[F[_]] extends VoteReadAPI[F[_]] with VoteWriteAPI[F[_]]
