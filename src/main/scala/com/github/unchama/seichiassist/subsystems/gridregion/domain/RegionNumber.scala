package com.github.unchama.seichiassist.subsystems.gridregion.domain

case class RegionNumber(value: Int) {
  require(value >= 0)

  def increment: RegionNumber = this.copy(value = value + 1)

}

object RegionNumber {

  /**
   * [[RegionNumber]]の初期値
   */
  val initial: RegionNumber = RegionNumber(0)

}
