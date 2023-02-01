package com.github.unchama.seichiassist.subsystems.gridregion.domain

import cats.implicits.catsSyntaxSemigroup
import com.github.unchama.generic.algebra.typeclasses.OrderedMonus
import com.github.unchama.seichiassist.subsystems.gridregion.domain.RelativeDirection._

/**
 * 1ユニット <=> 15 * 15ブロックの保護領域
 */
case class RegionUnit(units: Int) {
  require(units >= 0)

  /**
   * 1[[RegionUnit]]あたりのブロック数
   */
  private val unitPerBlockAmount = 15

  /**
   * [[RegionUnit]]からブロック数を計算する
   */
  def computeBlockAmount: Int = units * unitPerBlockAmount

}

object RegionUnit {

  val zero: RegionUnit = RegionUnit(0)

  implicit lazy val orderedMonus: OrderedMonus[RegionUnit] =
    new OrderedMonus[RegionUnit] {
      override def |-|(x: RegionUnit, y: RegionUnit): RegionUnit = RegionUnit(
        if (x.units > y.units) x.units - y.units else 0
      )

      override def empty: RegionUnit = zero

      override def compare(x: RegionUnit, y: RegionUnit): Int = x.units.compareTo(y.units)

      override def combine(x: RegionUnit, y: RegionUnit): RegionUnit = RegionUnit(
        x.units + y.units
      )
    }

}

case class RegionUnits(
  ahead: RegionUnit,
  right: RegionUnit,
  behind: RegionUnit,
  left: RegionUnit
) {

  import com.github.unchama.generic.algebra.typeclasses.OrderedMonus._

  /**
   * @return [[RegionUnits]]の中から`relativeDirection`に紐づく[[RegionUnit]]を返す
   */
  def fromRelativeDirectionToRegionUnit(relativeDirection: RelativeDirection): RegionUnit =
    relativeDirection match {
      case RelativeDirection.Ahead  => ahead
      case RelativeDirection.Behind => behind
      case RelativeDirection.Left   => left
      case RelativeDirection.Right  => right
    }

  /**
   * @return `relativeDirection`方向の[[RegionUnit]]を`extension`だけ拡張した[[RegionUnits]]
   */
  def expansionRegionUnits(
    relativeDirection: RelativeDirection,
    extension: RegionUnit
  ): RegionUnits =
    relativeDirection match {
      case Ahead  => this.copy(ahead = ahead |+| extension)
      case Behind => this.copy(behind = behind |+| extension)
      case Left   => this.copy(left = left |+| extension)
      case Right  => this.copy(right = right |+| extension)
    }

  /**
   * @return `relativeDirection`方向の[[RegionUnit]]を`extension`だけ縮小した[[RegionUnits]]
   */
  def contractRegionUnits(
    relativeDirection: RelativeDirection,
    extension: RegionUnit
  ): RegionUnits =
    relativeDirection match {
      case Ahead  => this.copy(ahead = ahead |-| extension)
      case Behind => this.copy(behind = behind |-| extension)
      case Left   => this.copy(left = left |-| extension)
      case Right  => this.copy(right = right |-| extension)
    }

  /*
    グリット保護は「プレイヤーがどれだけ範囲を拡大するか」を定義するので、
    最初に保護をしようとした時点で1ユニットの保護をすることになる。
    そのために1ユニット分だけ最後に加算をする。
   */
  def computeTotalRegionUnits: RegionUnit = {
    val vertical = (ahead |+| behind).units
    val horizontal = (right |+| left).units
    RegionUnit(vertical * horizontal) |+| RegionUnit(1)
  }

}

object RegionUnits {

  val initial: RegionUnits =
    RegionUnits(RegionUnit.zero, RegionUnit.zero, RegionUnit.zero, RegionUnit.zero)

}
