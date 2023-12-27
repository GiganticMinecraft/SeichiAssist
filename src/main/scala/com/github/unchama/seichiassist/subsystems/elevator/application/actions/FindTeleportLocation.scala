package com.github.unchama.seichiassist.subsystems.elevator.application.actions

trait FindTeleportLocation[F[_], Location] {

  /**
   * @return `targetLocation`がテレポート先として正しいかどうかを判定する作用
   */
  def isTeleportTargetLocation(targetLocation: Location): F[Boolean]

  /**
   * @return `currentLocation`より上のテレポート対象となる[[Location]]を探す作用
   */
  def findUpperLocation(currentLocation: Location): F[Option[Location]]

  /**
   * @return `currentLocation`より下のテレポート対象となる[[Location]]を探す作用
   */
  def findLowerLocation(currentLocation: Location): F[Option[Location]]

}
