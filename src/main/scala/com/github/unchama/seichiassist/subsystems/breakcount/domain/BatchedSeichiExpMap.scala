package com.github.unchama.seichiassist.subsystems.breakcount.domain

import com.github.unchama.generic.MapExtra
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount

class BatchedSeichiExpMap[Player] private (private val map: Map[Player, SeichiExpAmount]) {

  def combine(pair: (Player, SeichiExpAmount)): BatchedSeichiExpMap[Player] = {
    val (player, amount) = pair

    val newMap = map.updatedWith(player)(amountOption =>
      Some(amountOption.map(_.add(amount)).getOrElse(amount))
    )

    new BatchedSeichiExpMap(newMap)
  }

  /**
   * Uuidごとにバッチに纏められた整地量マップを取得する。
   *
   * NOTE: CraftBukkitにおいては、Uuid比較よりも厳しいエンティティId比較がプレーヤーのequals比較によって行われている。
   * これは整地量集計において厳しすぎる等価性のため、 [[HasUuid]] が提供するUuid情報での等価性によってコレクションをまとめ直す。
   */
  def toUuidCollatedList(
    implicit playerHasUuid: HasUuid[Player]
  ): List[(Player, SeichiExpAmount)] =
    MapExtra.collapseKeysThrough(playerHasUuid.of)(map).toList

}

object BatchedSeichiExpMap {

  def empty[Player]: BatchedSeichiExpMap[Player] = new BatchedSeichiExpMap(Map.empty)

}
