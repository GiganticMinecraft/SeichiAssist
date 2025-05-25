package com.github.unchama.seichiassist.subsystems.gridregion.domain

trait GetRegionCountLimit[F[_], World] {

  /**
   * １つのワールドに一人のプレイヤーが作成できる [[RegionCount]] の上限を返す作用
   */
  def apply(world: World): F[RegionCount]
}
