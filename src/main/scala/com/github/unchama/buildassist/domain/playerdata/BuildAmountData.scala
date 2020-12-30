package com.github.unchama.buildassist.domain.playerdata

import com.github.unchama.buildassist.domain.explevel.{BuildAssistExpTable, BuildExpAmount, BuildLevel}

/**
 * BuildAssistが管理する建築量データ。
 */
case class BuildAmountData(expAmount: BuildExpAmount) {

  /**
   * 建築量に対応する建築レベル。
   */
  lazy val levelCorrespondingToExp: BuildLevel =
    BuildAssistExpTable.levelAt(expAmount)

  def modifyExpAmount(f: BuildExpAmount => BuildExpAmount): BuildAmountData = copy(expAmount = f(expAmount))

}

object BuildAmountData {

  val initial: BuildAmountData = BuildAmountData(BuildExpAmount(BigDecimal(0)))

}
