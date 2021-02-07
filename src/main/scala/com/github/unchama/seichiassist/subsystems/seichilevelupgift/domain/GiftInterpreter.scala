package com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain

import cats.Applicative
import cats.data.Kleisli

trait GiftInterpreter[F[_], Player] {

  import cats.implicits._

  def grantEffectOfGift(gift: Gift): Kleisli[F, Player, Unit]

  final def grantEffectOnBundle(giftBundle: GiftBundle)(implicit F: Applicative[F]): Kleisli[F, Player, Unit] =
    giftBundle
      .map.toList
      .traverse { case (gift, i) => grantEffectOfGift(gift).replicateA(i) }
      .as(())

}
