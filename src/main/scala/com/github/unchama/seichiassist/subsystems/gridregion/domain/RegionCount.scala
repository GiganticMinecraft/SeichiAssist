package com.github.unchama.seichiassist.subsystems.gridregion.domain

case class RegionCount(value: Int) {
  require(value >= 0)

  def increment: RegionCount = this.copy(value = value + 1)

}

object RegionCount {

  /**
   * [[RegionCount]]の初期値
   */
  val initial: RegionCount = RegionCount(0)

}
