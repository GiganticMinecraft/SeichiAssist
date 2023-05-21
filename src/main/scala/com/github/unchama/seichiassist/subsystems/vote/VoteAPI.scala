package com.github.unchama.seichiassist.subsystems.vote

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.vote.domain._

import java.util.UUID

trait VoteWriteAPI[F[_]] {

  /**
   * @return effectPointを減少させる作用
   */
  def decreaseEffectPoint(uuid: UUID, effectPoint: EffectPoint): F[Unit]

}

object VoteWriteAPI {

  def apply[F[_]](implicit ev: VoteWriteAPI[F]): VoteWriteAPI[F] = ev

}

trait VoteReadAPI[F[_], Player] {

  /**
   * @return 投票回数を返す作用
   */
  def count(uuid: UUID): F[VoteCount]

  /**
   * @return 連続投票日数を返す作用
   */
  def currentConsecutiveVoteStreakDays(uuid: UUID): F[ChainVoteDayNumber]

  /**
   * @return effectPointを返す作用
   */
  def effectPoints(player: Player): F[EffectPoint]

  /**
   * @return 投票特典を受け取った回数を返す作用
   */
  def receivedCount(uuid: UUID): F[ReceivedVoteCount]

}

object VoteReadAPI {

  def apply[F[_], Player](implicit ev: VoteReadAPI[F, Player]): VoteReadAPI[F, Player] = ev

}

trait VoteReceiveAPI[F[_], Player] {

  /**
   * @return 投票特典を受け取る作用
   */
  def receiveVoteBenefits: Kleisli[F, Player, Unit]

}

object VoteReceiveAPI {

  def apply[F[_], Player](implicit ev: VoteReceiveAPI[F, Player]): VoteReceiveAPI[F, Player] =
    ev

}

trait VoteAPI[F[_], Player]
    extends VoteReadAPI[F, Player]
    with VoteWriteAPI[F]
    with VoteReceiveAPI[F, Player]
