package com.github.unchama.minecraft.actions

import cats.Functor

trait GetConnectedPlayers[F[_], Player] {

  /**
   * 現在サーバーに接続しているプレーヤーのリストを取得する作用。
   */
  val now: F[List[Player]]

  /**
   * 現在サーバーに接続しているプレーヤー数を取得する作用。
   */
  final def currentCount(implicit F: Functor[F]): F[Int] = F.map(now)(_.size)

}

object GetConnectedPlayers {

  def apply[F[_], Player](
    implicit ev: GetConnectedPlayers[F, Player]
  ): GetConnectedPlayers[F, Player] = ev

}
