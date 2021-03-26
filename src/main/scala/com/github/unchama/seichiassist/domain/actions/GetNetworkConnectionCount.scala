package com.github.unchama.seichiassist.domain.actions

trait GetNetworkConnectionCount[F[_]] {

  /**
   * 整地鯖全体に接続しているプレーヤーの数を取得する作用。
   */
  val now: F[Int]

}

object GetNetworkConnectionCount {

  def apply[F[_]](implicit ev: GetNetworkConnectionCount[F]): GetNetworkConnectionCount[F] = ev

}
