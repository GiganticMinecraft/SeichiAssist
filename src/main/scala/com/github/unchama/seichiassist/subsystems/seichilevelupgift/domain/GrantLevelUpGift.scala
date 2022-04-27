package com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain

import cats.data.Kleisli

trait GrantLevelUpGift[F[_], Player] {
  def grant(gift: Gift): Kleisli[F, Player, Unit]
}
