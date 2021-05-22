package com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata

import cats.Order
import cats.kernel.Monoid
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.{BuildAssistExpTable, BuildExpAmount, BuildLevel}

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

  implicit val order: Order[BuildAmountData] = Order.by(_.expAmount)

  implicit val monoid: Monoid[BuildAmountData] =
    Monoid.instance(initial, (a, b) => BuildAmountData(a.expAmount.add(b.expAmount)))
}
