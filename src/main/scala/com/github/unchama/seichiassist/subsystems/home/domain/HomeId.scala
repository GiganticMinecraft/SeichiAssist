package com.github.unchama.seichiassist.subsystems.home.domain

import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData

case class HomeId(value: Int) {
  require(
    HomeId.minimumNumber <= value && value <= HomeId.maxNumber,
    s"HomeIdは${HomeId.minimumNumber}から${HomeId.maxNumber}の間である必要があります"
  )

  override def toString: String = value.toString

}

object HomeId {

  /**
   * [[HomeId]]が持つことができる最小値
   */
  val minimumNumber: 1 = 1

  /**
   * [[HomeId]]が持つことができる最大値
   */
  val maxNumber = Home.initialHomePerPlayer + maxNumberExtra

  /**
   * プレイヤーのレベルに応じて追加で持つことができる[[HomeId]]の数
   */
  private val maxNumberExtra: 8 = 8

  /**
   * 追加ホームポイント
   * - 整地レベルが増加することに1つ（50、100、200、星5）、計4つ
   * - 建築レベルが増加することに1つ（20、40、60、100）、計4つ
   *
   * @return そのプレイヤーが現在扱える[[HomeId]]の最大値
   */
  def maxNumberByPlayerOf(seichiAmount: SeichiAmountData, buildAmount: BuildAmountData): Int = {
    val seichiLevel = seichiAmount.levelCorrespondingToExp.level
    val additionalHomePointBySeichiLevel =
      Seq(50, 100, 200).foldLeft(0)((acm, elem) => if (seichiLevel >= elem) acm + 1 else acm)
    val additionalHomePointByStarLevel =
      if (seichiAmount.starLevelCorrespondingToExp.level >= 5) 1 else 0
    val buildLevel = buildAmount.levelCorrespondingToExp.level
    val additionalHomePointByBuildLevel =
      Seq(20, 40, 60, 100).foldLeft(0)((acm, elem) => if (buildLevel >= elem) acm + 1 else acm)

    additionalHomePointBySeichiLevel + additionalHomePointByStarLevel + additionalHomePointByBuildLevel
  }
}
