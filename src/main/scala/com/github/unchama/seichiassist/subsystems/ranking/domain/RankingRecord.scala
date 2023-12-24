package com.github.unchama.seichiassist.subsystems.ranking.domain

import java.util.UUID

case class RankingRecord[V](playerName: String, uuid: UUID, value: V)

case class RankingRecordWithPosition[V](record: RankingRecord[V], positionInRanking: Int)
