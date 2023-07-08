package com.github.unchama.seichiassist.subsystems.gridregion.domain

import com.github.unchama.generic.ListExtra
import com.github.unchama.seichiassist.subsystems.gridregion.domain.HorizontalAxisAlignedRelativeDirection._

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
  require(0f <= yaw && yaw <= 360f)
}

/**
 * @param start 始点(範囲に含まれる)
 * @param end 終点(範囲に含まれない)
 */
case class YawRange(start: Yaw, end: Yaw)

object YawRange {

  def apply(start: Float, end: Float): YawRange = YawRange(Yaw(start), Yaw(end))

}

/**
 * 水平軸上の相対的な方向を定義したenum
 */
sealed trait HorizontalAxisAlignedRelativeDirection

object HorizontalAxisAlignedRelativeDirection {

  case object Ahead extends HorizontalAxisAlignedRelativeDirection

  case object Behind extends HorizontalAxisAlignedRelativeDirection

  case object Left extends HorizontalAxisAlignedRelativeDirection

  case object Right extends HorizontalAxisAlignedRelativeDirection

}

/**
 * 方角の範囲を列挙するenum用のabstract class
 * @param range 方角の範囲
 */
abstract class CardinalDirection(val uiLabel: String, private val range: YawRange*) {
  require(range.nonEmpty)

  /**
   * @return `yaw`が`range`の範囲内ならばtrueを返す
   */
  def isWithinRange(yaw: Float): Boolean =
    range.exists(range => range.start.yaw <= yaw && yaw <= range.end.yaw)

}

object CardinalDirection {

  case object North extends CardinalDirection("北(North)", YawRange(-180f, -135f), YawRange(135f, 180f))

  case object East extends CardinalDirection("東(East)", YawRange(-135f, -45f))

  case object South extends CardinalDirection("南(South)", YawRange(-45f, 45f))

  case object West extends CardinalDirection("西(West)", YawRange(45f, 135))

  /**
   * `yaw`から[[CardinalDirection]]に変換する
   */
  private def convertYawToDirection(yaw: Float): CardinalDirection = {
    if (North.isWithinRange(yaw)) North
    else if (East.isWithinRange(yaw)) East
    else if (South.isWithinRange(yaw)) South
    else West
  }

  /**
   * @return 現在向いている方向(`yaw`)から、相対的な方向([[HorizontalAxisAlignedRelativeDirection]])と
   *         紐づいている方角([[CardinalDirection]])を返す。
   */
  def relativeToCardinalDirections(
    yaw: Float
  ): Map[HorizontalAxisAlignedRelativeDirection, CardinalDirection] = {
    val directions = List(North, East, South, West)
    val horizontalAxisAlignedRelativeDirections = List(Ahead, Right, Behind, Left)

    val rotatedDirections =
      ListExtra.rotateLeftUntil(directions)(_ == convertYawToDirection(yaw)).get

    (horizontalAxisAlignedRelativeDirections zip rotatedDirections).toMap
  }

}
