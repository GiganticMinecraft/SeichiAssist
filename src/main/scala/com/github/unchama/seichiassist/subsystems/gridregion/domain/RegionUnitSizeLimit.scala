package com.github.unchama.seichiassist.subsystems.gridregion.domain

/**
 * ワールド内で作成できる保護領域の最大ユニット数。
 */
case class RegionUnitSizeLimit(limit: RegionUnitCount) {
  require(limit.count >= 0)
}
