package com.github.unchama.seichiassist.subsystems.home.domain

import cats.effect.{ConcurrentEffect, SyncEffect}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.buildcount.BuildCountAPI
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData
import org.bukkit.entity.Player

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
   * プレイヤーのレベルに応じて追加で持つことができる[[HomeId]]の数
   */
  private val maxNumberExtra: 8 = 8

  /**
   * [[HomeId]]が持つことができる最大値
   */
  val maxNumber = Home.initialHomePerPlayer + maxNumberExtra

  /**
   * 追加ホームポイント
   * - 整地レベルが増加することに1つ（50、100、200、星5）、計4つ
   * - 建築レベルが増加することに1つ（20、40、60、100）、計4つ
   *
   * @return 引数の整地量、建築量の状態で現在扱える[[HomeId]]の最大値
   */
  def maxNumberByExpOf(seichiAmount: SeichiAmountData, buildAmount: BuildAmountData): Int = {
    val seichiLevelThreshold = Set(50, 100, 200)
    val buildLevelThreshold = Set(20, 40, 60, 100)
    val seichiLevel = seichiAmount.levelCorrespondingToExp.level
    val additionalHomePointBySeichiLevel =
      seichiLevelThreshold.count(rawLevel => seichiLevel >= rawLevel)
    val additionalHomePointByStarLevel =
      if (seichiAmount.starLevelCorrespondingToExp.level >= 5) 1 else 0
    val buildLevel = buildAmount.levelCorrespondingToExp.level
    val additionalHomePointByBuildLevel =
      buildLevelThreshold.count(rawLevel => buildLevel >= rawLevel)

    additionalHomePointBySeichiLevel + additionalHomePointByStarLevel + additionalHomePointByBuildLevel
  }

  /**
   * プレイヤーの現在レベル（整地レベル、建築レベル）で利用可能なホームポイントIDの最大値を取得する作用
   */
  def maxHomeIdCanBeUsedF[F[_]: ConcurrentEffect, G[_]: SyncEffect: ContextCoercion[*[_], F]](
    player: Player
  )(
    implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player],
    buildCountReadAPI: BuildCountAPI[F, G, Player]
  ): F[Int] = {
    import cats.implicits._
    for {
      seichiAmount <- ContextCoercion(breakCountReadAPI.seichiAmountDataRepository(player).read)
      buildAmount <- ContextCoercion(buildCountReadAPI.playerBuildAmountRepository(player).read)
    } yield Home.initialHomePerPlayer + maxNumberByExpOf(seichiAmount, buildAmount)
  }

}
