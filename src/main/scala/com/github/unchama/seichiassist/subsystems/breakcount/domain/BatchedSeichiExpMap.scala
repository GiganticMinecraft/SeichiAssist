package com.github.unchama.seichiassist.subsystems.breakcount.domain

import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount

case class BatchedSeichiExpMap[Player](map: Map[Player, SeichiExpAmount]) {

  def combine(pair: (Player, SeichiExpAmount)): BatchedSeichiExpMap[Player] =
    BatchedSeichiExpMap {
      val (player, amount) = pair

      map.updatedWith(player)(amountOption =>
        Some(amountOption.map(_.add(amount)).getOrElse(amount))
      )
    }

}

object BatchedSeichiExpMap {

  def empty[Player]: BatchedSeichiExpMap[Player] = BatchedSeichiExpMap(Map.empty)

}
