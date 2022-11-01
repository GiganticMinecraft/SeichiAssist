package com.github.unchama.seichiassist.subsystems.home.domain

case class HomeLocation(
  worldName: String,
  x: Double,
  y: Double,
  z: Double,
  pitch: Float,
  yaw: Float
)

/**
 * ホームオブジェクトのクラス
 */
case class Home(name: Option[String], location: HomeLocation)

object Home {

  /**
   * プレイヤーが初期状態で持つことができるホームポイントの個数
   */
  val initialHomePerPlayer: 10 = 10
}
