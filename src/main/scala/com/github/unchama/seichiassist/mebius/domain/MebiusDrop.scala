package com.github.unchama.seichiassist.mebius.domain

import cats.effect.Sync
import com.github.unchama.seichiassist.mebius.domain.property.MebiusProperty

import scala.util.Random

object MebiusDrop {

  // Mebiusドロップ率
  // パラメータpの幾何分布の平均は1/pであるから、
  // 1ブロック壊すごとに 1 / averageBlocksToBeBrokenPerMebiusDrop の確率でドロップが起これば
  // 平均 averageBlocksToBeBrokenPerMebiusDrop 回の試行でドロップすることになる。
  private val averageBlocksToBeBrokenPerMebiusDrop = 50000

  def tryOnce[F[_] : Sync](ownerName: String, ownerUuid: String): F[Option[MebiusProperty]] = {
    Sync[F].delay {
      if (Random.nextInt(averageBlocksToBeBrokenPerMebiusDrop) == 0) {
        Some(MebiusProperty.initialProperty(ownerName, ownerUuid))
      } else {
        None
      }
    }
  }

}
