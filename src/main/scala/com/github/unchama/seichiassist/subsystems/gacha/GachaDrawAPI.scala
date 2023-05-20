package com.github.unchama.seichiassist.subsystems.gacha

import cats.data.Kleisli

trait GachaDrawAPI[F[_], Player] {

  /**
   * @return ガチャを引く作用
   */
  def drawGacha(draws: Int): Kleisli[F, Player, Unit]

}

object GachaDrawAPI {

  def apply[F[_], Player](implicit ev: GachaDrawAPI[F, Player]): GachaDrawAPI[F, Player] =
    ev

}
