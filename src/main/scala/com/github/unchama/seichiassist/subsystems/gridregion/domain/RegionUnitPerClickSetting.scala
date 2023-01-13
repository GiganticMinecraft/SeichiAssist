package com.github.unchama.seichiassist.subsystems.gridregion.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

class RegionUnitPerClickSetting[F[_]: Sync] {

  private val unitPerClickReference: Ref[F, RegionUnit] = Ref.unsafe(RegionUnit(1))

  import cats.implicits._

  /**
   * @return 1回のクリックで増減させる[[RegionUnit]]の量を取得する作用
   */
  def unitPerClick: F[RegionUnit] = for {
    regionUnitValue <- unitPerClickReference.get
  } yield regionUnitValue

  private val regionUnitOrder: Map[RegionUnit, RegionUnit] =
    Map(1 -> 10, 10 -> 100, 100 -> 1).map {
      case (first, second) => RegionUnit(first) -> RegionUnit(second)
    }

  /**
   * @return 1回のクリックで増減させる[[RegionUnit]]の量をトグルする作用
   */
  def toggleUnitPerClick: F[Unit] = for {
    currentUnitPerClick <- unitPerClick
    _ <- unitPerClickReference.set(regionUnitOrder(currentUnitPerClick))
  } yield ()

}
