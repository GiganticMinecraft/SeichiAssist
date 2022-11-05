package com.github.unchama.seichiassist.subsystems.home.domain

import cats.effect.{ConcurrentEffect, SyncEffect}
import cats.implicits._
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.buildcount.BuildCountAPI

case class HomeLocation(
  worldName: String,
  x: Double,
  y: Double,
  z: Double,
  pitch: Float,
  yaw: Float
) {
  // [x|y|z]Int は座標を負の方向に切り捨てで丸める(Debug画面のBlock:で表示される座標と同じ丸め方)
  val xInt: Int = x.floor.toInt
  val yInt: Int = y.floor.toInt
  val zInt: Int = z.floor.toInt
}

/**
 * ホームオブジェクトのクラス
 */
case class Home(name: Option[String], location: HomeLocation)

object Home {

  /**
   * プレイヤーが初期状態で持つことができるホームポイントの個数
   */
  val initialHomePerPlayer: 10 = 10

  /**
   * プレイヤーの現在レベル（整地レベル、建築レベル）で利用可能なホームポイント数を取得する作用
   */
  def maxAvailableHomeCountF[F[_]: ConcurrentEffect, G[_]: SyncEffect: ContextCoercion[
    *[_],
    F
  ], Player](player: Player)(
    implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player],
    buildCountReadAPI: BuildCountAPI[F, G, Player]
  ): F[Int] = {
    for {
      seichiAmount <- ContextCoercion(breakCountReadAPI.seichiAmountDataRepository(player).read)
      buildAmount <- ContextCoercion(buildCountReadAPI.playerBuildAmountRepository(player).read)
    } yield Home.initialHomePerPlayer + HomeId.maxNumberByExpOf(seichiAmount, buildAmount)
  }

}
