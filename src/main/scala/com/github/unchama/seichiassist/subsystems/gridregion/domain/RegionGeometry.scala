package com.github.unchama.seichiassist.subsystems.gridregion.domain

import cats.implicits._
import com.github.unchama.generic.algebra.typeclasses.OrderedMonus
import com.github.unchama.seichiassist.subsystems.gridregion.domain.HorizontalAxisAlignedSubjectiveDirection._

/**
 * グリッド保護システムが基本とする 15m (Minecraft内の15ブロック分の長さ) を "1 region unit length" (RUL) と定義した上での、
 * region unit length での長さの非負整数量。
 */
case class RegionUnitLength(rul: Int) {
  require(rul >= 0)

  /**
   * この長さ量をメートルでの長さ表現に変換する。
   */
  def toMeters: Int = 15 * rul
}

object RegionUnitLength {
  implicit lazy val orderedMonus: OrderedMonus[RegionUnitLength] =
    new OrderedMonus[RegionUnitLength] {
      override def |-|(x: RegionUnitLength, y: RegionUnitLength): RegionUnitLength =
        RegionUnitLength(if (x.rul > y.rul) x.rul - y.rul else 0)

      override def empty: RegionUnitLength = RegionUnitLength(0)

      override def compare(x: RegionUnitLength, y: RegionUnitLength): Int =
        x.rul.compareTo(y.rul)

      override def combine(x: RegionUnitLength, y: RegionUnitLength): RegionUnitLength =
        RegionUnitLength(x.rul + y.rul)
    }
}

/**
 * 保護領域 (Region) 内に「保護ユニット」が何個並んでいるのかという正の無次元量。
 *
 * 「保護ユニット」は、1 RUL × 1 RUL の正方形の領域形と定義される。
 * そのうえで、「保護領域」は、1つ以上の保護ユニットを長方形状に敷き詰めた領域として定義される。
 * したがって、`RegionUnit(n)` は、n個の保護ユニットを長方形状に敷き詰めた領域内の保護ユニット数を指す。
 */
case class RegionUnitCount(count: Int) {
  require(count >= 1)
}

/**
 * プレーヤーから見た主観的な方向によって規定される、長方形状の保護領域形。
 *
 * 例えば、 ahead, right, behind, left がそれぞれ 1, 2, 3, 4 RUL として規定される形は、
 * <pre>
 *   OOOOOOO
 *   OOOOXOO
 *   OOOOOOO
 *   OOOOOOO
 *   OOOOOOO
 * </pre> のような俯瞰図で表される保護領域 (ただし、プレーヤーは上記の領域の X の位置に図の上を向いて立っており、
 * O はプレーヤーがいないが保護領域に入った保護ユニットである) に対応する。
 */
case class SubjectiveRegionShape(
  ahead: RegionUnitLength,
  right: RegionUnitLength,
  behind: RegionUnitLength,
  left: RegionUnitLength
) {

  import com.github.unchama.generic.algebra.typeclasses.OrderedMonus._

  /**
   * @return この領域形の `relativeDirection` 方向への長さ。
   */
  def lengthInto(
    relativeDirection: HorizontalAxisAlignedSubjectiveDirection
  ): RegionUnitLength =
    relativeDirection match {
      case HorizontalAxisAlignedSubjectiveDirection.Ahead  => ahead
      case HorizontalAxisAlignedSubjectiveDirection.Behind => behind
      case HorizontalAxisAlignedSubjectiveDirection.Left   => left
      case HorizontalAxisAlignedSubjectiveDirection.Right  => right
    }

  /**
   * @return この領域形を `relativeDirection` 方向に `length` だけ拡張した [[SubjectiveRegionShape]]
   */
  def extendTowards(
    relativeDirection: HorizontalAxisAlignedSubjectiveDirection
  )(length: RegionUnitLength): SubjectiveRegionShape =
    relativeDirection match {
      case Ahead  => this.copy(ahead = ahead |+| length)
      case Behind => this.copy(behind = behind |+| length)
      case Left   => this.copy(left = left |+| length)
      case Right  => this.copy(right = right |+| length)
    }

  /**
   * @return この領域形を `relativeDirection` 方向に `length` だけ縮小した [[SubjectiveRegionShape]]
   */
  def contractAlong(
    relativeDirection: HorizontalAxisAlignedSubjectiveDirection
  )(length: RegionUnitLength): SubjectiveRegionShape =
    relativeDirection match {
      case Ahead  => this.copy(ahead = ahead |-| length)
      case Behind => this.copy(behind = behind |-| length)
      case Left   => this.copy(left = left |-| length)
      case Right  => this.copy(right = right |-| length)
    }

  /**
   * @return この領域形内の保護ユニット数
   */
  def regionUnits: RegionUnitCount = {
    val vertical = ahead |+| behind |+| RegionUnitLength(1)
    val horizontal = right |+| left |+| RegionUnitLength(1)
    RegionUnitCount(vertical.rul * horizontal.rul)
  }
}

object SubjectiveRegionShape {
  val initial: SubjectiveRegionShape =
    SubjectiveRegionShape(
      RegionUnitLength(0),
      RegionUnitLength(0),
      RegionUnitLength(0),
      RegionUnitLength(0)
    )
}
