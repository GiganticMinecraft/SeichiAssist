package com.github.unchama.seichiassist.subsystems.awayscreenname.domain

import cats.effect.concurrent.Ref

trait PlayerLocationRepository[F[_], Location, Player] {

  protected val locationRepository: Ref[F, PlayerLocation[Location]]

  /**
   * @return リポジトリの値を受け取ったプレイヤーの[[Location]]に更新する作用
   */
  def updateNowLocation(): F[Unit]

  /**
   * @return リポジトリから値を取得する作用
   */
  def getRepositoryLocation: F[PlayerLocation[Location]] = locationRepository.get

}
