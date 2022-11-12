package com.github.unchama.seichiassist.subsystems.gridregion

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.gridregion.domain.{
  CreateRegionResult,
  Direction,
  RegionSelection,
  RegionUnit,
  RegionUnitLimit,
  RegionUnits
}

trait GridRegionAPI[F[_], Player, Location] {

  /**
   * @return 1回のクリックで増減させる[[RegionUnit]]の量をトグルする作用を返す
   */
  def toggleUnitPerClick: Kleisli[F, Player, Unit]

  /**
   * @return 1回のクリックで増減させる[[RegionUnit]]の量を返す作用
   */
  def unitPerClick(player: Player): F[RegionUnit]

  /**
   * @return 指定された`worldName`の[[RegionUnitLimit]]
   */
  def regionUnitLimit(worldName: String): RegionUnitLimit

  /**
   * @return 指定された[[RegionUnits]]から保護が作成できる限界値を超えていないか返す作用
   */
  def isWithinLimits(regionUnits: RegionUnits, worldName: String): Boolean

  /**
   * @return [[Player]]の[[RegionUnits]]を取得する作用
   */
  def regionUnits(player: Player): F[RegionUnits]

  /**
   * @return [[Player]]の[[RegionUnits]]を上書きする作用
   */
  def saveRegionUnits(regionUnits: RegionUnits): Kleisli[F, Player, Unit]

  /**
   * @return [[Player]]が[[RegionUnits]]分だけ保護を作成できるかどうかを返す作用
   */
  def canCreateRegion(
    player: Player,
    regionUnits: RegionUnits,
    direction: Direction
  ): CreateRegionResult

  /**
   * @return 保護を作成する作用
   */
  def createRegion: Kleisli[F, Player, Unit]

  /**
   * @return `player`の現在地点と`regionUnits`から[[RegionSelection]]を計算して返す
   */
  def regionSelection(
    player: Player,
    regionUnits: RegionUnits,
    direction: Direction
  ): RegionSelection[Location]

}
