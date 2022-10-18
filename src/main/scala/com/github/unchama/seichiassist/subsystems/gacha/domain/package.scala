package com.github.unchama.seichiassist.subsystems.gacha

import cats.effect.concurrent.Ref
import com.github.unchama.seichiassist.subsystems.gacha.domain.gachaprize.GachaPrize

package object domain {

  type GlobalGachaPrizeList[F[_], ItemStack] = Ref[F, Vector[GachaPrize[ItemStack]]]

}
