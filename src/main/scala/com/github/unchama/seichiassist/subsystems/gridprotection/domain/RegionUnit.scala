package com.github.unchama.seichiassist.subsystems.gridprotection.domain

/**
 * 1ユニット <=> 15 * 15の保護領域
 */
case class RegionUnit(value: Int) {
  require(value >= 0)
}

object RegionUnit {

  /**
   * Unitの初期値
   */
  val initial: RegionUnit = RegionUnit(0)

}
