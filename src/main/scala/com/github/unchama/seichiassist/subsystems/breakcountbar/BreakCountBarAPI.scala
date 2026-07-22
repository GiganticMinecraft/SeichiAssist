package com.github.unchama.seichiassist.subsystems.breakcountbar

import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.subsystems.breakcountbar.domain.BreakCountBarVisibility
import cats.effect.Ref

trait BreakCountBarAPI[F[_], Player] {

  val breakCountBarVisibility: KeyedDataRepository[Player, Ref[F, BreakCountBarVisibility]]

}
