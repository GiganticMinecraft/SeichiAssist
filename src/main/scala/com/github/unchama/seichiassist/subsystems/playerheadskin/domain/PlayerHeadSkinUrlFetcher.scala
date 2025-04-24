package com.github.unchama.seichiassist.subsystems.playerheadskin.domain

trait PlayerHeadSkinUrlFetcher[F[_]] {

  /**
   * @return `playerName`の頭のスキンのURLを取ってくる作用
   */
  def fetchHeadSkinUrl(playerName: String): F[Option[HeadSkinUrl]]

}
