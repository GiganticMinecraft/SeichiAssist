package com.github.unchama.seichiassist.subsystems.gridregion.domain

trait RegionNumberPersistence[F[_], Player] {

  /**
   * @return `player`の[[RegionNumber]]を`regionNumber`で更新する作用
   */
  def setRegionNumber(player: Player, regionNumber: RegionNumber): F[Unit]

  /**
   * @return `player`の[[RegionNumber]]を取得する作用
   */
  def fetchRegionNumber(player: Player): F[Unit]

}
