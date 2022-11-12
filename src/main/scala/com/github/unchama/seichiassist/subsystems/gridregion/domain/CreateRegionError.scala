package com.github.unchama.seichiassist.subsystems.gridregion.domain

sealed trait CreateRegionError

/**
 * 保護範囲を作成するとき発生しうるエラーを列挙したオブジェクト
 */
object CreateRegionError {

  /**
   * このワールドでは保護が作成できない
   */
  case object ThisWorldRegionCanNotBeCreated extends CreateRegionError

  /**
   * それ以外のエラー(以下のエラー)で保護が作成できない
   *  - 他保護との重複
   *  - 保護の作成上限に達している
   */
  case object RegionCanNotBeCreatedByOtherError extends CreateRegionError

}
