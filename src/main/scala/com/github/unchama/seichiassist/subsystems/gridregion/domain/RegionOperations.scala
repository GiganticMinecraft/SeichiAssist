package com.github.unchama.seichiassist.subsystems.gridregion.domain

case class RegionSelectionCorners[Location](startPosition: Location, endPosition: Location)

trait RegionOperations[F[_], Location, Player] {

  /**
   * @return `currentLocation` から `shape` を使って保護範囲の始点と終点を求める
   */
  def getSelectionCorners(
    currentLocation: Location,
    shape: SubjectiveRegionShape
  ): RegionSelectionCorners[Location]

  /**
   * @return プレーヤーが現在選択している WorldGuard 領域により WorldGuard 保護の作成を試みる作用
   */
  def tryCreatingSelectedWorldGuardRegion(player: Player): F[Unit]

  /**
   * @return `player` が `shape` の形をした保護範囲を作成できるかを確認する作用
   */
  def canCreateRegion(player: Player, shape: SubjectiveRegionShape): F[RegionCreationResult]

}
