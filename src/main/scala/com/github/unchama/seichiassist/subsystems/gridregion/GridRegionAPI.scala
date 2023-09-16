package com.github.unchama.seichiassist.subsystems.gridregion

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.gridregion.domain._

trait GridRegionTemplateAPI[F[_], Player] {

  /**
   * @return `player`が保存した[[RegionTemplate]]の一覧を返す作用
   */
  def savedGridRegionTemplates(player: Player): F[Vector[RegionTemplate]]

  /**
   * @return [[Player]]が[[RegionTemplate]]を保存する作用
   */
  def saveGridRegionTemplate(regionTemplate: RegionTemplate): Kleisli[F, Player, Unit]

}

trait GridRegionReadAPI[F[_], Player, Location] {

  /**
   * @return 1回のクリックで増減させる[[RegionUnitLength]]を返す作用
   */
  def lengthChangePerClick(player: Player): F[RegionUnitLength]

  /**
   * @return 指定された`worldName`の[[RegionUnitSizeLimit]]
   */
  def regionUnitLimit(worldName: String): RegionUnitSizeLimit

  /**
   * @return [[Player]]が現在設定している[[SubjectiveRegionShape]]を取得する作用
   */
  def currentlySelectedShape(player: Player): F[SubjectiveRegionShape]

  /**
   * @return [[Player]]が[[SubjectiveRegionShape]]分だけ保護を作成できるかどうかを返す作用
   */
  def canCreateRegion(player: Player, shape: SubjectiveRegionShape): F[RegionCreationResult]

  /**
   * @return `player`の現在地点と`regionUnits`から[[RegionSelectionCorners]]を計算して返す
   */
  def regionSelection(
    player: Player,
    shape: SubjectiveRegionShape
  ): RegionSelectionCorners[Location]

  /**
   * @return `player`の[[RegionCount]]を取得する作用
   */
  def regionCount(player: Player): F[RegionCount]

}

trait GridRegionWriteAPI[F[_], Player, Location] {

  /**
   * @return [[SubjectiveRegionShape]] を調整する UI 上の１回のクリックで増減させる[[RegionUnitLength]]の量をトグルする作用
   */
  def toggleRulChangePerClick: Kleisli[F, Player, Unit]

  /**
   * @return [[Player]] が持つ [[SubjectiveRegionShape]] 設定を上書きする作用
   */
  def updateCurrentRegionShapeSettings(
    regionUnits: SubjectiveRegionShape
  ): Kleisli[F, Player, Unit]

  /**
   * @return プレーヤーが現在 WorldGuard で選択している領域にて保護を作成する作用
   */
  def createAndClaimRegionSelectedOnWorldGuard: Kleisli[F, Player, Unit]

}

trait GridRegionAPI[F[_], Player, Location]
    extends GridRegionTemplateAPI[F, Player]
    with GridRegionReadAPI[F, Player, Location]
    with GridRegionWriteAPI[F, Player, Location]
