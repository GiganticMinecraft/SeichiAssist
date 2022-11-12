package com.github.unchama.seichiassist.subsystems.gridregion.domain

trait RegionOperations[F[_], Player] {

  /**
   * @return `regionUnits`の範囲を選択する作用
   */
  def selectRegion(player: Player, regionUnits: RegionUnits): F[Unit]

}
