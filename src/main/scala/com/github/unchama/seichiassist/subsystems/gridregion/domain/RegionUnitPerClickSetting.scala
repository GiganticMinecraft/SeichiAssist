package com.github.unchama.seichiassist.subsystems.gridregion.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

class RegionUnitPerClickSetting[F[_]: Sync] {

  private val unitPerClickReference: Ref[F, RegionUnits] = Ref.unsafe(RegionUnits(1))

  import cats.implicits._

  /**
   * @return 1回のクリックで増減させる[[RegionUnits]]の量を取得する作用
   */
  def unitPerClick: F[RegionUnits] = for {
    regionUnitValue <- unitPerClickReference.get
  } yield regionUnitValue

  private val regionUnitOrder: Map[RegionUnits, RegionUnits] =
    Map(1 -> 10, 10 -> 100, 100 -> 1).map {
      case (first, second) => RegionUnits(first) -> RegionUnits(second)
    }

  /**
   * @return 1回のクリックで増減させる[[RegionUnits]]の量をトグルする作用
   */
  def toggleUnitPerClick: F[Unit] = for {
    currentUnitPerClick <- unitPerClick
    _ <- unitPerClickReference.set(regionUnitOrder(currentUnitPerClick))
  } yield ()

}
