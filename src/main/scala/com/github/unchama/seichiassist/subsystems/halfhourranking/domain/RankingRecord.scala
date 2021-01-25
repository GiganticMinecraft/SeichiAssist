package com.github.unchama.seichiassist.subsystems.halfhourranking.domain

import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount

/**
 * 30分ランキングの表示に必要なデータ。
 */
class RankingRecord[Player](internalMap: Map[Player, SeichiExpAmount]) {

  def addCount(player: Player, exp: SeichiExpAmount): RankingRecord[Player] = {
    val updatedMap = internalMap.updatedWith(player) {
      case Some(value) => Some(value.add(exp))
      case None => Some(exp)
    }

    new RankingRecord[Player](updatedMap)
  }

  def getSortedNonzeroRecords: List[(Player, SeichiExpAmount)] =
    internalMap
      .toList
      .filter(_._2 != SeichiExpAmount.zero)
      .sortBy(_._2)(SeichiExpAmount.orderedMonus.toOrdering)

}

object RankingRecord {

  def empty[Player]: RankingRecord[Player] = new RankingRecord[Player](Map.empty)

}
