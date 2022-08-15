package com.github.unchama.seichiassist.subsystems.gacha.application.actions

trait DrawGacha[F[_], Player] {

  def draw(player: Player, count: Int): F[Unit]

}

object DrawGacha {

  def apply[F[_], Player](implicit ev: DrawGacha[F, Player]): DrawGacha[F, Player] = ev

}
