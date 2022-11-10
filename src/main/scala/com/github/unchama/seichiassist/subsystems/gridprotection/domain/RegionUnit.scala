package com.github.unchama.seichiassist.subsystems.gridprotection.domain

/**
 * 1ユニット <=> 15 * 15の保護領域
 */
case class RegionUnit(units: Int) {
  require(units >= 0)

  /**
   * 指定[[RegionUnit]]分だけ加算する
   */
  def add(_units: RegionUnit): RegionUnit = RegionUnit(units + _units.units)

}

object RegionUnit {

  /**
   * Unitの初期値
   */
  val initial: RegionUnit = RegionUnit(0)

}
