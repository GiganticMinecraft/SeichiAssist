package com.github.unchama.seichiassist.subsystems.gridregion.domain

trait RegionDefiner[F[_], Location] {

  /**
   * @return `currentLocation` から `shape` を使って保護範囲の始点と終点を求める作用
   */
  def getSelectionCorners(
    currentLocation: Location,
    shape: SubjectiveRegionShape
  ): F[RegionSelectionCorners[Location]]
}
