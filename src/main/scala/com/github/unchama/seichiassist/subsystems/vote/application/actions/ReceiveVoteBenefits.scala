package com.github.unchama.seichiassist.subsystems.vote.application.actions

trait ReceiveVoteBenefits[F[_], G[_], Player] {

  def receive(player: Player): F[Unit]

}

object ReceiveVoteBenefits {

  def apply[F[_], G[_], Player](
    implicit ev: ReceiveVoteBenefits[F, G, Player]
  ): ReceiveVoteBenefits[F, G, Player] = ev

}
