package com.github.unchama.seichiassist.subsystems.gridregion.application

import com.github.unchama.seichiassist.subsystems.gridregion.domain.RegionUnitSizeLimit

trait GetGridLimitPerWorld[F[_]] {
  def apply(worldName: String): F[RegionUnitSizeLimit]
}
