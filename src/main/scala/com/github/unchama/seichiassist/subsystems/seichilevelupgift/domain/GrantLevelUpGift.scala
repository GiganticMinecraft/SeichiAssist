package com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi

trait GrantLevelUpGift[F[_], Target, G[_], ItemStack] {
  def grant(gift: Gift)(
    implicit gachaPointApi: GachaPointApi[F, G, Target],
    gachaAPI: GachaAPI[F, ItemStack]
  ): Kleisli[F, Target, Unit]
}

object GrantLevelUpGift {

  def apply[F[_], Player, G[_], ItemStack](
    implicit ev: GrantLevelUpGift[F, Player, G, ItemStack]
  ): GrantLevelUpGift[F, Player, G, ItemStack] = implicitly

}
