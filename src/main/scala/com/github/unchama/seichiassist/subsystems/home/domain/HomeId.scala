package com.github.unchama.seichiassist.subsystems.home.domain

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
  val maxNumber: 9 = Home.maxHomePerPlayer
}
