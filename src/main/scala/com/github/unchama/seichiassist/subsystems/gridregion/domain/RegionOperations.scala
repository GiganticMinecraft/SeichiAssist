package com.github.unchama.seichiassist.subsystems.gridregion.domain

trait RegionOperations[Location] {

  /**
   * @return `currentLocation`から`regionUnits`を使って保護範囲の始点と終点を求める
   */
  def getSelection(
    currentLocation: Location,
    regionUnits: RegionUnits,
    direction: Direction
  ): RegionSelection[Location]

}
