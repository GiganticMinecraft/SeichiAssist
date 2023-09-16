package com.github.unchama.seichiassist.subsystems.gridregion.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

class RegionUnitPerClickSetting[F[_]: Sync] {

  private val unitPerClickReference: Ref[F, RegionUnitLength] = Ref.unsafe(RegionUnitLength(1))

  import cats.implicits._

  /**
   * @return 1回のクリックで増減させる[[RegionUnitLength]]の量を取得する作用
   */
  def unitPerClick: F[RegionUnitLength] = for {
    regionUnitValue <- unitPerClickReference.get
  } yield regionUnitValue

  private val regionUnitLengthOrder: Map[RegionUnitLength, RegionUnitLength] =
    Map(1 -> 10, 10 -> 100, 100 -> 1).map {
      case (first, second) => RegionUnitLength(first) -> RegionUnitLength(second)
    }

  /**
   * @return 1回のクリックで増減させる[[RegionUnitLength]]の量をトグルする作用
   */
  def toggleUnitPerClick: F[Unit] = for {
    currentUnitPerClick <- unitPerClick
    _ <- unitPerClickReference.set(regionUnitLengthOrder(currentUnitPerClick))
  } yield ()

}
