package com.github.unchama.seichiassist.subsystems.gridregion.domain

sealed trait CreateRegionResult

/**
 * 保護範囲を作成するとき発生しうる結果を定義したobject
 */
object CreateRegionResult {

  /**
   * 保護が作成できる
   */
  case object Success extends CreateRegionResult

  /**
   * このワールドでは保護が作成できない
   */
  case object ThisWorldRegionCanNotBeCreated extends CreateRegionResult

  /**
   * それ以外のエラー(以下のエラー)で保護が作成できない
   *  - 他保護との重複
   *  - 保護の作成上限に達している
   */
  case object RegionCanNotBeCreatedByOtherError extends CreateRegionResult

}
