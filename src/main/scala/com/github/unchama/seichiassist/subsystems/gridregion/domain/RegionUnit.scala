package com.github.unchama.seichiassist.subsystems.gridregion.domain

/**
 * 1ユニット <=> 15 * 15ブロックの保護領域
 */
case class RegionUnit(units: Int) {
  require(units >= 0)

  /**
   * 指定[[RegionUnit]]分だけ加算する
   */
  def add(_units: RegionUnit): RegionUnit = RegionUnit(units + _units.units)

  /**
   * 1[[RegionUnit]]あたりのブロック数
   */
  private val blockPerBlockAmount = 15

  /**
   * [[RegionUnit]]からブロック数を計算する
   */
  def computeBlockAmount: Int = units * blockPerBlockAmount

}

object RegionUnit {

  /**
   * [[RegionUnit]]の初期値
   */
  val initial: RegionUnit = RegionUnit(0)

}
