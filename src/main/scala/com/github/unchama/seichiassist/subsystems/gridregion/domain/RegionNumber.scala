package com.github.unchama.seichiassist.subsystems.gridregion.domain

case class RegionNumber(value: Int) {
  require(value >= 0)
}

object RegionNumber {

  /**
   * [[RegionNumber]]の初期値
   */
  val initial: RegionNumber = RegionNumber(0)

}
