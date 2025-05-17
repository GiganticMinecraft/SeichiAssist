package com.github.unchama.seichiassist.subsystems.gridregion.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

/**
 * [[SubjectiveRegionShape]] の変更を行う UI で、クリック毎に主観的領域選択をどの程度の長さ伸縮させるかの設定。
 */
class RULChangePerClickSetting[F[_]: Sync] private (
  private val changePerClickRef: Ref[F, RegionUnitLength]
) {

  import cats.implicits._

  /**
   * @return 1回のクリックで増減させる[[RegionUnitLength]]を取得する作用
   */
  def rulChangePerClick: F[RegionUnitLength] = for {
    regionUnitValue <- changePerClickRef.get
  } yield regionUnitValue

  private val settingsCycle: Map[RegionUnitLength, RegionUnitLength] =
    Map(1 -> 10, 10 -> 100, 100 -> 1).map {
      case (first, second) => RegionUnitLength(first) -> RegionUnitLength(second)
    }

  /**
   * @return 1回のクリックで増減させる[[RegionUnitLength]]の量をトグルする作用
   */
  def toggleUnitPerClick: F[Unit] = changePerClickRef.getAndUpdate(settingsCycle.apply).void

}

object RULChangePerClickSetting {
  import cats.implicits._

  def apply[F[_]: Sync]: F[RULChangePerClickSetting[F]] =
    Ref.of[F, RegionUnitLength](RegionUnitLength(1)).map(new RULChangePerClickSetting(_))
}
