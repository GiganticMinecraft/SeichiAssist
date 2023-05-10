package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions

trait RecoveryMana[F[_]] {

  /**
   * @return 妖精がマナを回復する作用
   */
  def recovery: F[Unit]

}

object RecoveryMana {

  def apply[F[_]](implicit ev: RecoveryMana[F]): RecoveryMana[F] = ev

}
