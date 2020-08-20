package com.github.unchama.seichiassist.mebius.service

import cats.effect.IO
import com.github.unchama.seichiassist.mebius.domain.property.MebiusProperty

import scala.util.Random

object MebiusDroppingService {

  // Mebiusドロップ率
  // パラメータpの幾何分布の平均は1/pであるから、
  // 1ブロック壊すごとに 1 / averageBlocksToBeBrokenPerMebiusDrop の確率でドロップが起これば
  // 平均 averageBlocksToBeBrokenPerMebiusDrop 回の試行でドロップすることになる。
  private val averageBlocksToBeBrokenPerMebiusDrop = 50000

  def tryForDrop(ownerName: String, ownerUuid: String): IO[Option[MebiusProperty]] = IO {
    if (Random.nextInt(averageBlocksToBeBrokenPerMebiusDrop) == 0) {
      Some(MebiusProperty.initialProperty(ownerName, ownerUuid))
    } else {
      None
    }
  }

}
