package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions

trait SummonFairy[F[_], G[_], Player] {

  /**
   * 妖精を召喚する作用
   */
  def summon(player: Player): F[Unit]

}

object SummonFairy {

  def apply[F[_], G[_], Player](
    implicit ev: SummonFairy[F, G, Player]
  ): SummonFairy[F, G, Player] =
    ev

}