package com.github.unchama.seichiassist.subsystems.gridregion.domain

import com.github.unchama.generic.ListExtra
import com.github.unchama.seichiassist.subsystems.gridregion.domain.HorizontalAxisAlignedSubjectiveDirection._

/**
 *  この`yaw`は、SpigotAPIが提供している値と合わせている。
 *  ref: https://helpch.at/docs/1.12/org/bukkit/Location.html#getYaw--
 *
 *  SpigotAPIから提供される`yaw`は、具体的に以下のようなものである。
 *   - 南を起点に0から始まる
 *   - 時計回りに北までで180となる
 *   - 反時計周りで北までで-180となる
 */
case class Yaw(yaw: Float) {
  require(-180f <= yaw && yaw <= 180f)
}

/**
 * 方角の半開区間。始点から時計回り (「南西北東」と回る方向) に進み終点に行きつくまでの角度の範囲を表す。
 *
 * @param start 始点(範囲に含まれる)
 * @param end 終点(範囲に含まれない)
 */
case class ClockwiseYawRange(start: Yaw, end: Yaw) {

  /**
   * この [[ClockwiseYawRange]] (の閉包) が 180 度線(= -180 度線) を跨いでいるかどうか。
   */
  val crossesNorthboundMeridian: Boolean = end.yaw < start.yaw

  /**
   * 与えられた角度がこの区間に入っているかどうか。
   */
  def contains(yaw: Yaw): Boolean = {
    if (crossesNorthboundMeridian) {
      start.yaw <= yaw.yaw || yaw.yaw < end.yaw || yaw.yaw == 180f
    } else {
      start.yaw <= yaw.yaw && yaw.yaw < end.yaw
    }
  }
}

object ClockwiseYawRange {

  def apply(start: Float, end: Float): ClockwiseYawRange =
    ClockwiseYawRange(Yaw(start), Yaw(end))

}

/**
 * 水平軸上の主観的な方向を定義したenum
 */
sealed trait HorizontalAxisAlignedSubjectiveDirection

object HorizontalAxisAlignedSubjectiveDirection {
  case object Ahead extends HorizontalAxisAlignedSubjectiveDirection
  case object Behind extends HorizontalAxisAlignedSubjectiveDirection
  case object Left extends HorizontalAxisAlignedSubjectiveDirection
  case object Right extends HorizontalAxisAlignedSubjectiveDirection
}

/**
 * 方角の範囲を列挙するenum用のabstract class
 * @param yawRange 方角の範囲
 */
abstract class CardinalDirection(
  val uiLabel: String,
  private[CardinalDirection] val yawRange: ClockwiseYawRange
)

object CardinalDirection {

  case object North extends CardinalDirection("北(North)", ClockwiseYawRange(135f, -135f))
  case object East extends CardinalDirection("東(East)", ClockwiseYawRange(-135f, -45f))
  case object South extends CardinalDirection("南(South)", ClockwiseYawRange(-45f, 45f))
  case object West extends CardinalDirection("西(West)", ClockwiseYawRange(45f, 135))

  /**
   * `yaw`から[[CardinalDirection]]に変換する
   */
  private def convertYawToDirection(yaw: Yaw): CardinalDirection = {
    if (North.yawRange.contains(yaw)) North
    else if (East.yawRange.contains(yaw)) East
    else if (South.yawRange.contains(yaw)) South
    else West
  }

  /**
   * @return 現在向いている方向(`aheadYaw`)から、相対的な方向([[HorizontalAxisAlignedSubjectiveDirection]])と
   *         紐づいている方角([[CardinalDirection]])を返す。
   */
  def relativeToCardinalDirections(
    aheadYaw: Float
  ): Map[HorizontalAxisAlignedSubjectiveDirection, CardinalDirection] = {
    val aheadDirection = convertYawToDirection(Yaw(aheadYaw))
    val directions = List(North, East, South, West)
    val horizontalAxisAlignedRelativeDirections = List(Ahead, Right, Behind, Left)

    val rotatedDirections = ListExtra.rotateLeftUntil(directions)(_ == aheadDirection).get
    (horizontalAxisAlignedRelativeDirections zip rotatedDirections).toMap
  }

}
