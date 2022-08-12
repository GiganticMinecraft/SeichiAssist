package com.github.unchama.seichiassist.subsystems.gacha.domain

import cats.effect.concurrent.Ref

object GlobalGachaPrizeList {

  type GlobalGachaPrizeList[F[_], ItemStack] = Ref[F, Vector[GachaPrize[ItemStack]]]

}
