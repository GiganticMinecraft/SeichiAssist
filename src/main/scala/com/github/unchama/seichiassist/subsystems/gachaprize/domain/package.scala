package com.github.unchama.seichiassist.subsystems.gachaprize

import cats.effect.Ref

package object domain {

  type GlobalGachaPrizeList[F[_], ItemStack] = Ref[F, Vector[GachaPrizeTableEntry[ItemStack]]]

}
