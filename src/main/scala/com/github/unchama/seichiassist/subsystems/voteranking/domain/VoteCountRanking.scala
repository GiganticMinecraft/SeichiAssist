package com.github.unchama.seichiassist.subsystems.voteranking.domain

class VoteCountRanking(records: Vector[VoteCountRankingRecord]) {
  private val sortedRecords = records.sortBy(_.count).reverse

  val recordCount: Int = records.size

  // 30000人のプレイヤーが30000日連続で投票したとしても9億回にしかならないので当分はこれで大丈夫
  val totalVoteCount: Int = records
    .map(_.count)
    .sum

  val recordsWithPositions: Vector[(Int, VoteCountRankingRecord)] = {
    val grouped1 = sortedRecords.groupMap(_.count)(_.playerName)
    grouped1.map { case (c, ps) =>
      // 投票回数から順位に張り直す。このときタイは同位とし、
      // より下位のプレイヤーの順位はタイが存在した分だけ余分に下がる。
      (grouped1.count { case (ic, _) => c < ic } + 1, c, ps)
    }.flatMap { case (rank, count, names) =>
      names.map((rank, count, _))
    }.map { case (rank, count, name) =>
      (rank, VoteCountRankingRecord(name, count))
    }.toVector
  }

  def positionAndRecordOf(playerName: String): Option[(Int, VoteCountRankingRecord)] =
    recordsWithPositions
      .find { case (_, record) => record.playerName == playerName }

  def positionOf(playerName: String): Option[Int] =
    positionAndRecordOf(playerName).map(_._1)
}
