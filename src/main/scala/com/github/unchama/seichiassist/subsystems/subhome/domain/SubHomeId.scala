package com.github.unchama.seichiassist.subsystems.subhome.domain

case class SubHomeId(value: Int) {
  require(
    SubHomeId.minimumNumber <= value && value <= SubHomeId.maxNumber,
    s"SubHomeIdは${SubHomeId.minimumNumber}から${SubHomeId.maxNumber}の間である必要があります"
  )

  override def toString: String = value.toString

}

object SubHomeId {

  /**
   * SubHomeIdが持つことができる最小値
   */
  val minimumNumber: 1 = 1

  /**
   * SubHomeIdが持つことができる最大値
   */
  val maxNumber: 9 = SubHome.maxSubHomePerPlayer
}
