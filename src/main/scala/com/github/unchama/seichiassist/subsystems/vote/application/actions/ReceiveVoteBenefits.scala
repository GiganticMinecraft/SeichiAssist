package com.github.unchama.seichiassist.subsystems.vote.application.actions

trait ReceiveVoteBenefits[F[_], Player] {

  /**
   * @return 投票特典を受け取る作用
   */
  def receive(player: Player): F[Unit]

}

object ReceiveVoteBenefits {

  def apply[F[_], Player](
    implicit ev: ReceiveVoteBenefits[F, Player]
  ): ReceiveVoteBenefits[F, Player] = ev

}
