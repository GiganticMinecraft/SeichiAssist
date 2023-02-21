package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions

trait SummonFairy[F[_], Player] {

  /**
   * @return 妖精を召喚する作用
   */
  def summon(player: Player): F[Unit]

}

object SummonFairy {

  def apply[F[_], Player](implicit ev: SummonFairy[F, Player]): SummonFairy[F, Player] =
    ev

}
