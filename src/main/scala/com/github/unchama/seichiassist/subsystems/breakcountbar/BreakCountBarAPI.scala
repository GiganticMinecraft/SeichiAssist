package com.github.unchama.seichiassist.subsystems.breakcountbar

import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.subsystems.breakcountbar.domain.BreakCountBarVisibility

trait BreakCountBarAPI[F[_], Player] {

  val breakCountBarVisibility: KeyedDataRepository[Player, Ref[F, BreakCountBarVisibility]]

}
