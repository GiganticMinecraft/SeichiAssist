package com.github.unchama.seichiassist.subsystems.gridregion.application.actions

import com.github.unchama.seichiassist.subsystems.gridregion.domain.RegionSelectionCorners

trait CreateRegion[F[_], Player, Location] {

  /**
   * @return `player` が `corners` の範囲で保護を作成する作用
   */
  def apply(player: Player, corners: RegionSelectionCorners[Location]): F[Unit]
}
