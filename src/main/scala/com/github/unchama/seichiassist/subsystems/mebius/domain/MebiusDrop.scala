package com.github.unchama.seichiassist.subsystems.mebius.domain

import cats.{Functor, Monad}
import com.github.unchama.seichiassist.subsystems.seasonalevents.christmas.Christmas
import com.github.unchama.seichiassist.subsystems.mebius.domain.property.{ChristmasMebius, MebiusProperty, NormalMebius}
import com.github.unchama.seichiassist.subsystems.seasonalevents.api.ChristmasEventsAPI
import com.github.unchama.util.RandomEffect

object MebiusDrop {

  // Mebiusドロップ率
  // パラメータpの幾何分布の平均は1/pであるから、
  // 1ブロック壊すごとに 1 / averageBlocksToBeBrokenPerMebiusDrop の確率でドロップが起これば
  // 平均 averageBlocksToBeBrokenPerMebiusDrop 回の試行でドロップすることになる。
  // TODO クリスマスイベントでのクリスマスMebius関係のゴタゴタの埋め合わせとしてドロップ確率を上げたが、クリスマスイベントが終わったら戻す
  private def averageBlocksToBeBrokenPerMebiusDrop[F[_] : Functor : ChristmasEventsAPI] = {
    Functor[F].ifF(
      ChristmasEventsAPI[F].isInEvent
    )(33333, 50000)
  }

  import cats.implicits._

  def tryOnce[F[_] : RandomEffect : ChristmasEventsAPI : Monad](ownerName: String,
                                                                ownerUuid: String): F[Option[MebiusProperty]] = {
    for {
      dropRate <- averageBlocksToBeBrokenPerMebiusDrop[F]
      dropping <- RandomEffect[F].tryForOneIn(dropRate)
      isInChristmasEvent <- ChristmasEventsAPI[F].isInEvent
    } yield {
      if (dropping) {
        val mebiusType = if (isInChristmasEvent) ChristmasMebius else NormalMebius
        Some(MebiusProperty.initialProperty(mebiusType, ownerName, ownerUuid))
      } else {
        None
      }
    }
  }

}
