package com.github.unchama.seichiassist.subsystems.gridregion.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

class RegionNumberRepository[F[_]: Sync] {

  private val regionNumberReference: Ref[F, RegionNumber] = Ref.unsafe(RegionNumber.initial)

  /**
   * @return `regionNumber`の値を設定する作用
   */
  def setRegionNumber(regionNumber: RegionNumber): F[Unit] =
    regionNumberReference.set(regionNumber)

  /**
   * @return 現在の[[RegionNumber]]を取得する作用
   */
  def regionNumber: F[RegionNumber] = regionNumberReference.get

  /**
   * @return 現在の[[RegionNumber]]に対してインクリメントする作用
   */
  def increment: F[Unit] = regionNumberReference.update(ref => ref.copy(value = ref.value + 1))

}
