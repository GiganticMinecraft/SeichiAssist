package com.github.unchama.seichiassist.subsystems.breakcount.application.actions

trait UpdateProgressBar[F[_], Player] {

  def of(player: Player)

}

object UpdateProgressBar {

  def apply[F[_], Player](implicit ev: UpdateProgressBar[F, Player]): UpdateProgressBar[F, Player] = ev

}
