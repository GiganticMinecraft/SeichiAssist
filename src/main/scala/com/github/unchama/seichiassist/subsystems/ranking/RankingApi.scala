package com.github.unchama.seichiassist.subsystems.ranking

/**
 *
 * @tparam F 作用の文脈
 * @tparam R [[com.github.unchama.seichiassist.subsystems.ranking.domain.SeichiRanking SeichiRanking]]のようなレコードをまとめる型
 */
trait RankingApi[F[_], R] {

  val getRanking: F[R]

}

