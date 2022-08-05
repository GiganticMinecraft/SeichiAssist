package com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain

import cats.data.Kleisli

trait GrantLevelUpGift[F[_], Target] {
  def grant(gift: Gift): Kleisli[F, Target, Unit]
}

object GrantLevelUpGift {

  def apply[F[_], Player](
    implicit ev: GrantLevelUpGift[F, Player]
  ): GrantLevelUpGift[F, Player] = implicitly

}
