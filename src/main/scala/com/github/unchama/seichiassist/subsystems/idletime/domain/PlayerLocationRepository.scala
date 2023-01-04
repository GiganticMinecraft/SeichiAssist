package com.github.unchama.seichiassist.subsystems.idletime.domain

import cats.effect.concurrent.Ref

trait PlayerLocationRepository[F[_], Location, Player] {

  protected val locationRepository: Ref[F, PlayerLocation[Location]]

  /**
   * @return リポジトリの値を新しい[[PlayerLocation]]に更新する作用
   */
  def updateNowLocation: F[Unit]

  /**
   * @return リポジトリから値を取得する作用
   */
  def getRepositoryLocation: F[PlayerLocation[Location]] = locationRepository.get

}
