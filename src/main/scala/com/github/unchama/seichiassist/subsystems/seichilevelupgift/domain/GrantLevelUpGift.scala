package com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi

trait GrantLevelUpGift[F[_], Target, G[_]] {
  def grant(gift: Gift)(
    implicit gachaPointApi: GachaPointApi[F, G, Target]
  ): Kleisli[F, Target, Unit]
}

object GrantLevelUpGift {

  def apply[F[_], Player, G[_]](
    implicit ev: GrantLevelUpGift[F, Player, G]
  ): GrantLevelUpGift[F, Player, G] = implicitly

}
