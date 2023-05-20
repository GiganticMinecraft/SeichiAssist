package com.github.unchama.seichiassist.subsystems.ranking.domain

case class RankingRecord[V](playerName: String, value: V)

case class RankingRecordWithPosition[V](record: RankingRecord[V], positionInRanking: Int)
