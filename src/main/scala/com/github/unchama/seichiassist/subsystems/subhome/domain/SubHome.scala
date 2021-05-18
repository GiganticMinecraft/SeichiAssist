package com.github.unchama.seichiassist.subsystems.subhome.domain

case class SubHomeLocation(worldName: String, x: Int, y: Int, z: Int)

/**
 * サブホームオブジェクトのクラス
 */
case class SubHome(name: Option[String], location: SubHomeLocation)
