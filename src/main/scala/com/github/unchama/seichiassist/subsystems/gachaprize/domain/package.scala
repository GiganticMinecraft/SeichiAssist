package com.github.unchama.seichiassist.subsystems.gachaprize

import cats.effect.concurrent.Ref
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaPrize

package object domain {

  type GlobalGachaPrizeList[F[_], ItemStack] = Ref[F, Vector[GachaPrize[ItemStack]]]

}
