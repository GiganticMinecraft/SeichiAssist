package com.github.unchama.seichiassist.subsystems.gridregion.domain

/**
 * @param start 始点
 * @param end 終点
 */
case class DirectionRange(start: Float, end: Float)

/**
 * 方角の範囲を列挙するenum用のabstract class
 * @param range 方角の範囲
 */
abstract class Direction private (range: DirectionRange*) {
  require(range.nonEmpty)

  /**
   * @return `yaw`が`range`の範囲内ならばtrueを返す
   */
  def isWithinRange(yaw: Float): Boolean =
    range.exists(range => range.start <= yaw && yaw <= range.end)
}

object Direction {

  case object North extends Direction(DirectionRange(0f, 45f), DirectionRange(316f, 360f))

  case object East extends Direction(DirectionRange(46f, 135f))

  case object South extends Direction(DirectionRange(136f, 225f))

  case object West extends Direction(DirectionRange(226f, 315f))

  /**
   * `yaw`から[[Direction]]に変換します。
   *
   * `yaw`は以下の定義が成り立つものとする。
   *  - 南を起点に0から始まる
   *  - 時計回りに北までで180となる
   *  - 反時計周りで北までで-180となる
   */
  def convertYawToDirection(yaw: Float): Direction = {
    // yawに+180することで北を起点とし、1周で360となる。
    val revisionYaw = yaw + 180

    if (North.isWithinRange(revisionYaw)) North
    else if (East.isWithinRange(revisionYaw)) East
    else if (South.isWithinRange(revisionYaw)) South
    else West
  }

}
