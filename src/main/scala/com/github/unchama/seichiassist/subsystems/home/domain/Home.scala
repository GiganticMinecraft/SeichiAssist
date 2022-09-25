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
   * プレイヤーが持つことができるホームの最大個数
   */
  val maxHomePerPlayer: 9 = 9
}
