package com.github.unchama.seichiassist.subsystems.gridregion

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.gridregion.domain.RegionUnit

trait GridRegionAPI[F[_], Player] {

  /**
   * @return 1回のクリックで増減させる[[RegionUnit]]の量をトグルする作用を返す
   */
  def toggleUnitPerClick: Kleisli[F, Player, Unit]

  /**
   * @return 1回のクリックで増減させる[[RegionUnit]]の量を返す作用
   */
  def unitPerClick(player: Player): F[RegionUnit]

}
