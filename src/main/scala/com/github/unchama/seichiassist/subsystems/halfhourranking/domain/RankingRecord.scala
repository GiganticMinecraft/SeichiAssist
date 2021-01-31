package com.github.unchama.seichiassist.subsystems.halfhourranking.domain

import com.github.unchama.seichiassist.subsystems.breakcount.domain.BatchedSeichiExpMap
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount

/**
 * 30分ランキングの表示に必要なデータ。
 */
case class RankingRecord[Player](batch: BatchedSeichiExpMap[Player]) {

  def getSortedNonzeroRecords: List[(Player, SeichiExpAmount)] =
    batch.map.toList
      .filter(_._2 != SeichiExpAmount.zero)
      .sortBy(_._2)(SeichiExpAmount.orderedMonus.toOrdering.reverse)

}
