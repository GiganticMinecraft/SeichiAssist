package com.github.unchama.seichiassist.subsystems.gridregion.domain

sealed trait RegionCreationResult

/**
 * グリッド保護領域を作成するとき発生しうる結果を定義したobject
 */
object RegionCreationResult {

  /**
   * グリッド保護が作成された
   */
  case object Success extends RegionCreationResult

  /**
   * このワールドではグリッド保護の作成が許可されておらず、作成できなかった
   */
  case object WorldProhibitsRegionCreation extends RegionCreationResult

  /**
   * 保護の作成は該当ワールド内で許可されているが、次のような理由により、保護が作成できなかった。
   *  - 他保護との重複
   *  - 保護の作成上限に達している
   */
  case object Error extends RegionCreationResult

}
