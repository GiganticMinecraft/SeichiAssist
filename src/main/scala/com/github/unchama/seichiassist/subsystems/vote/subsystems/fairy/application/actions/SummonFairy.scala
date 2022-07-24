package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions

trait SummonFairy[F[_]] {

  /**
   * 妖精を召喚する作用
   */
  def summon: F[Unit]

}

object SummonFairy {

  def apply[F[_]](implicit ev: SummonFairy[F]): SummonFairy[F] = ev

}
