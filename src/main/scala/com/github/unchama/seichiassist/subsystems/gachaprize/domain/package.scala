package com.github.unchama.seichiassist.subsystems.gachaprize

import cats.effect.concurrent.Ref

package object domain {

  type GlobalGachaPrizeList[F[_], ItemStack] = Ref[F, Vector[GachaPrize[ItemStack]]]

}
