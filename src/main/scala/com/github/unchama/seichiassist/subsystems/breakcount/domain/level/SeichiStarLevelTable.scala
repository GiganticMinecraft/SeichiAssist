package com.github.unchama.seichiassist.subsystems.breakcount.domain.level

/**
 * 経験値量と整地スターレベルの相互変換を与えるオブジェクト。
 *
 * 必要な整地経験値量は整地スターレベルに比例し、
 * 比例係数は [[SeichiLevelTable]] が定義する最大レベルに到達した瞬間の経験値量に等しい。
 */
object SeichiStarLevelTable {

  val gradient: SeichiExpAmount = SeichiLevelTable.table.expAt(SeichiLevelTable.table.maxLevel)

  /**
   * 与えられた経験値量で到達している最大の整地スターレベルを計算する。
   */
  def levelAt(expAmount: SeichiExpAmount): SeichiStarLevel = SeichiStarLevel.ofNonNegative {
    (expAmount.amount / gradient.amount).toInt
  }

  /**
   * 与えられた整地スターレベルへの到達に必要な経験値量を計算する。
   */
  def expAt(starLevel: SeichiStarLevel): SeichiExpAmount = SeichiExpAmount.ofNonNegative {
    gradient.amount * starLevel.level
  }

}
