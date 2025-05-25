package com.github.unchama.seichiassist.subsystems.gridregion.domain

trait GetGridUnitSizeLimitPerWorld[F[_], World] {
  def apply(world: World): F[RegionUnitSizeLimit]
}
