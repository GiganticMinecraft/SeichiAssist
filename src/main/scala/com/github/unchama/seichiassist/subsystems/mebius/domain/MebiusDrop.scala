package com.github.unchama.seichiassist.subsystems.mebius.domain

import cats.Apply
import com.github.unchama.seichiassist.subsystems.mebius.domain.property.{
  ChristmasMebius,
  MebiusProperty,
  NormalMebius
}
import com.github.unchama.seichiassist.subsystems.seasonalevents.api.ChristmasEventsAPI
import com.github.unchama.util.RandomEffect

object MebiusDrop {

  // Mebiusドロップ率
  // パラメータpの幾何分布の平均は1/pであるから、
  // 1ブロック壊すごとに 1 / averageBlocksToBeBrokenPerMebiusDrop の確率でドロップが起これば
  // 平均 averageBlocksToBeBrokenPerMebiusDrop 回の試行でドロップすることになる。
  private val averageBlocksToBeBrokenPerMebiusDrop = 50000

  def tryOnce[F[_]: RandomEffect: ChristmasEventsAPI: Apply](
    ownerName: String,
    ownerUuid: String
  ): F[Option[MebiusProperty]] =
    Apply[F].map2(
      RandomEffect[F].tryForOneIn(averageBlocksToBeBrokenPerMebiusDrop),
      ChristmasEventsAPI[F].isInEvent
    ) {
      case (dropping, isChristmas) =>
        if (dropping) {
          val mebiusType = if (isChristmas) ChristmasMebius else NormalMebius
          Some(MebiusProperty.initialProperty(mebiusType, ownerName, ownerUuid))
        } else {
          None
        }
    }

}
