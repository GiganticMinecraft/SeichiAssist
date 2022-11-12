package com.github.unchama.seichiassist.subsystems.gridregion.domain

import com.github.unchama.seichiassist.subsystems.gridregion.domain.RelativeDirection._

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
   * 指定[[RegionUnit]]分だけ減算する
   */
  def subtract(_units: RegionUnit): RegionUnit = RegionUnit(units - _units.units)

  /**
   * 1[[RegionUnit]]あたりのブロック数
   */
  val unitPerBlockAmount = 15

  /**
   * [[RegionUnit]]からブロック数を計算する
   */
  def computeBlockAmount: Int = units * unitPerBlockAmount

}

object RegionUnit {

  /**
   * @return [[RegionUnit]]の初期値
   */
  val initial: RegionUnit = RegionUnit(0)

}

case class RegionUnits(
  ahead: RegionUnit,
  right: RegionUnit,
  behind: RegionUnit,
  left: RegionUnit
) {

  /**
   * @return `relativeDirection`方向の[[RegionUnit]]を`extension`だけ拡張した[[RegionUnits]]
   */
  def expansionRegionUnits(
    relativeDirection: RelativeDirection,
    extension: RegionUnit
  ): RegionUnits =
    relativeDirection match {
      case Ahead  => this.copy(ahead = ahead.add(extension))
      case Behind => this.copy(behind = behind.add(extension))
      case Left   => this.copy(left = left.add(extension))
      case Right  => this.copy(right = right.add(extension))
    }

  /**
   * @return `relativeDirection`方向の[[RegionUnit]]を`extension`だけ縮小した[[RegionUnits]]
   */
  def contractRegionUnits(
    relativeDirection: RelativeDirection,
    extension: RegionUnit
  ): RegionUnits =
    relativeDirection match {
      case Ahead  => this.copy(ahead = ahead.subtract(extension))
      case Behind => this.copy(behind = behind.subtract(extension))
      case Left   => this.copy(left = left.subtract(extension))
      case Right  => this.copy(right = right.subtract(extension))
    }

  /*
    グリット保護は「プレイヤーがどれだけ範囲を拡大するか」を定義するので、
    最初に保護をしようとした時点で1ユニットの保護をすることになる。
    そのために1ユニット分だけ最後に加算をする。
   */
  def computeTotalRegionUnits: RegionUnit =
    RegionUnit(ahead.add(behind).units * right.add(left).units).add(RegionUnit(1))

}

object RegionUnits {

  val initial: RegionUnits =
    RegionUnits(RegionUnit.initial, RegionUnit.initial, RegionUnit.initial, RegionUnit.initial)

}
