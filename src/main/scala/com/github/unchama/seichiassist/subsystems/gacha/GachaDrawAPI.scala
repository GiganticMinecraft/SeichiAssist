package com.github.unchama.seichiassist.subsystems.gacha

trait GachaDrawAPI[F[_], Player] {

  /**
   * @return ガチャを実行する作用
   */
  def drawGacha(player: Player, draws: Int): F[Unit]

}

object GachaDrawAPI {

  def apply[F[_], Player](implicit ev: GachaDrawAPI[F, Player]): GachaDrawAPI[F, Player] =
    ev

}
