package com.github.unchama.seichiassist.subsystems.gacha

import cats.effect.concurrent.Ref

package object domain {

  type GlobalGachaPrizeList[F[_], ItemStack] = Ref[F, Vector[gachaprize.GachaPrize[ItemStack]]]

}
