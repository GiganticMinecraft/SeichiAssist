package com.github.unchama.seichiassist.domain.playercount

trait GetConnectedPlayerCount[F[_]] {

  /**
   * 現在サーバーに接続しているプレーヤー数を取得する。
   */
  def now: F[Int]

}

object GetConnectedPlayerCount {

  def apply[F[_]](implicit ev: GetConnectedPlayerCount[F]): GetConnectedPlayerCount[F] = ev

}
