package com.github.unchama.seichiassist.subsystems.gridregion.domain

trait RegionOperations[F[_], Location, Player] {

  /**
   * @return `currentLocation`から`regionUnits`を使って保護範囲の始点と終点を求める
   */
  def getSelection(
    currentLocation: Location,
    regionUnits: RegionUnits,
    direction: Direction
  ): RegionSelection[Location]

  /**
   * @return 保護の作成を試みる作用
   */
  def tryCreateRegion(player: Player): F[Boolean]

  /**
   * @return 保護範囲を作成できるか確認する作用
   */
  def canCreateRegion(
    player: Player,
    regionUnits: RegionUnits,
    direction: Direction
  ): F[CreateRegionResult]

}
