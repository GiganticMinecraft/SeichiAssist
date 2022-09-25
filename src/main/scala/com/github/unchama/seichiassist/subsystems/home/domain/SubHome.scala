package com.github.unchama.seichiassist.subsystems.home.domain

case class SubHomeLocation(
  worldName: String,
  x: Double,
  y: Double,
  z: Double,
  pitch: Float,
  yaw: Float
)

/**
 * サブホームオブジェクトのクラス
 */
case class SubHome(name: Option[String], location: SubHomeLocation)

object SubHome {

  /**
   * プレイヤーが持つことができるサブホームの最大個数
   */
  val maxSubHomePerPlayer: 9 = 9
}
