package com.github.unchama.seichiassist.subsystems.gridregion.domain

import java.util.UUID

trait RegionNumberPersistence[F[_]] {

  /**
   * @return `uuid`の[[RegionNumber]]を`regionNumber`で更新する作用
   */
  def setRegionNumber(uuid: UUID, regionNumber: RegionNumber): F[Unit]

  /**
   * @return `uuid`の[[RegionNumber]]を取得する作用
   */
  def fetchRegionNumber(uuid: UUID): F[Unit]

}
