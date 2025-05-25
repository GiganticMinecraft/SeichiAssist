package com.github.unchama.seichiassist.subsystems.gridregion.domain

case class RegionSelectionCorners[Location](startPosition: Location, endPosition: Location)

trait RegionRegister[F[_], Location, Player] {

  /**
   * @return `player` が `shape` の形をした保護範囲を作成できるかを確認する作用
   */
  def canCreateRegion(player: Player, shape: SubjectiveRegionShape): F[RegionCreationResult]

}
