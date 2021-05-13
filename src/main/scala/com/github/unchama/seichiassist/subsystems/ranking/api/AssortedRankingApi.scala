package com.github.unchama.seichiassist.subsystems.ranking.api

import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData
import com.github.unchama.seichiassist.subsystems.loginranking.domain.LoginTime
import com.github.unchama.seichiassist.subsystems.ranking.domain.{Ranking, VoteCount}

/**
 * 定期的に更新されるランキングデータを提供するオブジェクトのtrait。
 *
 * @tparam F ランキングを取得する作用の文脈
 * @tparam R ランキングのレコードが保持するデータ型
 */
case class RankingProvider[F[_], R](ranking: ReadOnlyRef[F, Ranking[R]])

trait AssortedRankingApi[F[_]] {

  val seichiAmountRanking: RankingProvider[F, SeichiAmountData]

  val buildAmountRanking: RankingProvider[F, BuildAmountData]

  val loginTimeRanking: RankingProvider[F, LoginTime]

  val voteCountRanking: RankingProvider[F, VoteCount]

}
