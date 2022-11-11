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
   * @return [[RegionUnit]]の初期値
   */
  val initial: RegionUnit = RegionUnit(0)

  /**
   * 各方向の[[RegionUnit]]から合計の[[RegionUnit]]を計算する
   * @return 各方向の[[RegionUnit]]の合計値
   */
  def computeTotalRegionUnits(
    ahead: RegionUnit,
    right: RegionUnit,
    behind: RegionUnit,
    left: RegionUnit
  ): RegionUnit = {
    /*
      グリット保護は「プレイヤーがどれだけ範囲を拡大するか」を定義するので、
      最初に保護をしようとした時点で1ユニットの保護をすることになる。
      そのために1ユニット分だけ最後に加算をする。
     */
    RegionUnit(ahead.add(behind).units * right.add(left).units).add(RegionUnit(1))
  }

}
