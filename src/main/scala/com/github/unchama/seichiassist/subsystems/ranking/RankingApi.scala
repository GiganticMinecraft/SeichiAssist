package com.github.unchama.seichiassist.subsystems.ranking

import com.github.unchama.seichiassist.subsystems.ranking.domain.SeichiRanking

trait RankingApi[F[_]] {

  val getSeichiRanking: F[SeichiRanking]

}

