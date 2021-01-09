package com.github.unchama.seichiassist.subsystems.breakcount.domain

import com.github.unchama.seichiassist.subsystems.breakcount.domain.level._

case class SeichiAmountData(expAmount: SeichiExpAmount) {

  /**
   * 経験値量に対応する整地レベル。[[SeichiLevelTable]]により決定される。
   */
  lazy val levelCorrespondingToExp: SeichiLevel =
    SeichiLevelTable.table.levelAt(expAmount)

  /**
   * 経験値量に対応する整地スターレベル。[[SeichiStarLevelTable]]により決定される。
   */
  lazy val starLevelCorrespondingToExp: SeichiStarLevel = {
    SeichiStarLevelTable.levelAt(expAmount)
  }

  def addExpAmount(another: SeichiExpAmount): SeichiAmountData = SeichiAmountData(expAmount.add(another))

}
