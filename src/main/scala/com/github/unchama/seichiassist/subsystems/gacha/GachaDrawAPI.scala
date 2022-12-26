package com.github.unchama.seichiassist.subsystems.gacha

trait GachaDrawAPI[F[_], Player] {

  /**
   * @return ガチャを引く作用
   * TODO: drawGachaをKleisliにする
   */
  def drawGacha(player: Player, draws: Int): F[Unit]

}

object GachaDrawAPI {

  def apply[F[_], Player](implicit ev: GachaDrawAPI[F, Player]): GachaDrawAPI[F, Player] =
    ev

}
