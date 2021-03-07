package com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain

import cats.Applicative
import cats.data.Kleisli
import com.github.unchama.generic.Diff
import com.github.unchama.generic.algebra.typeclasses.HasSuccessor
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel

trait GiftInterpreter[F[_], Player] {

  import cats.implicits._

  def onGift(gift: Gift): Kleisli[F, Player, Unit]

  final def onBundle(giftBundle: GiftBundle)(implicit F: Applicative[F]): Kleisli[F, Player, Unit] =
    giftBundle
      .map.toList
      .traverse { case (gift, i) => onGift(gift).replicateA(i) }
      .as(())

  final def onLevelDiff(levelDiff: Diff[SeichiLevel])
                       (implicit F: Applicative[F]): Kleisli[F, Player, Unit] = {
    HasSuccessor[SeichiLevel]
      .leftOpenRightClosedRange(levelDiff.left, levelDiff.right)
      .toList
      .traverse { level => onBundle(GiftBundleTable.bundleAt(level)) }
      .as(())
  }
}
