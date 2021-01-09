package com.github.unchama.seichiassist.subsystems.breakcount.domain.level

import com.github.unchama.seichiassist.domain.explevel.ExpLevelConversion

/**
 * 経験値量と整地スターレベルの相互変換を与えるオブジェクト。
 *
 * 必要な整地経験値量は整地スターレベルに比例し、
 * 比例係数は [[SeichiLevelTable]] が定義する最大レベルに到達した瞬間の経験値量に等しい。
 */
object SeichiStarLevelTable extends ExpLevelConversion[SeichiStarLevel, SeichiExpAmount] {

  val gradient: SeichiExpAmount = SeichiLevelTable.table.expAt(SeichiLevelTable.table.maxLevel)

  override def levelAt(expAmount: SeichiExpAmount): SeichiStarLevel = SeichiStarLevel.ofNonNegative {
    (expAmount.amount / gradient.amount).toInt
  }

  override def expAt(starLevel: SeichiStarLevel): SeichiExpAmount = SeichiExpAmount.ofNonNegative {
    gradient.amount * starLevel.level
  }

}
