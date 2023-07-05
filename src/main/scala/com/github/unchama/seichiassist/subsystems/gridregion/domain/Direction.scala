package com.github.unchama.seichiassist.subsystems.gridregion.domain

import com.github.unchama.generic.MathExtra
import com.github.unchama.seichiassist.subsystems.gridregion.domain.RelativeDirection._

/**
 * @param start 始点(範囲に含まれる)
 * @param end 終点(範囲に含まれない)
 */
case class YawRange(start: Float, end: Float) {
  private def isWithinDirection(value: Float): Boolean = 0f <= value && value <= 360f

  require(isWithinDirection(start))
  require(isWithinDirection(end))
}

/**
 * 相対的な方向を定義したenum
 */
sealed trait RelativeDirection

object RelativeDirection {

  case object Ahead extends RelativeDirection

  case object Behind extends RelativeDirection

  case object Left extends RelativeDirection

  case object Right extends RelativeDirection

}

/**
 * 方角の範囲を列挙するenum用のabstract class
 * @param range 方角の範囲
 */
abstract class Direction(val uiLabel: String, private val range: YawRange*) {
  require(range.nonEmpty)

  /**
   * @return `yaw`が`range`の範囲内ならばtrueを返す
   */
  def isWithinRange(yaw: Float): Boolean =
    range.exists(range => range.start <= yaw && yaw <= range.end)

}

object Direction {

  case object North extends Direction("北(North)", YawRange(0f, 45f), YawRange(315f, 360f))

  case object East extends Direction("東(East)", YawRange(45f, 135f))

  case object South extends Direction("南(South)", YawRange(135f, 225f))

  case object West extends Direction("西(West)", YawRange(225f, 315f))

  /**
   * `yaw`から[[Direction]]に変換する
   *
   * `yaw`は以下の定義が成り立つものとする。
   *  - 南を起点に0から始まる
   *  - 時計回りに北までで180となる
   *  - 反時計周りで北までで-180となる
   */
  private def convertYawToDirection(yaw: Float): Direction = {
    // yawに+180することで北を起点とし、1周で360となる。
    val revisionYaw = yaw + 180

    if (North.isWithinRange(revisionYaw)) North
    else if (East.isWithinRange(revisionYaw)) East
    else if (South.isWithinRange(revisionYaw)) South
    else West
  }

  /**
   * @return 現在向いている方向(`yaw`)から、相対的な方向(`RelativeDirection`)と
   *         紐づいている方角(`Direction`)を返す。
   */
  def relativeDirection(yaw: Float): Map[RelativeDirection, Direction] = {
    val directionOrder: Map[Direction, Direction] =
      Map(North -> East, East -> South, South -> West, West -> North)
    val relativeDirectionOrder = List(Ahead, Right, Behind, Left)

    val computedDirectionOrder =
      MathExtra.recurrenceRelation(directionOrder, convertYawToDirection(yaw))()

    (relativeDirectionOrder zip computedDirectionOrder).toMap
  }

}
