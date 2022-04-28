package com.github.unchama.seichiassist.subsystems.subhome.domain

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
