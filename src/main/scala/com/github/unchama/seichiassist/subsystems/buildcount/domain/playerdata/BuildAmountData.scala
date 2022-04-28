package com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata

import cats.Order
import cats.kernel.Monoid
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.{
  BuildAssistExpTable,
  BuildExpAmount,
  BuildLevel,
  BuildLevelProgress
}

/**
 * BuildAssistが管理する建築量データ。
 */
case class BuildAmountData(expAmount: BuildExpAmount) {

  /**
   * 建築量に対応する建築レベル。
   */
  lazy val levelCorrespondingToExp: BuildLevel =
    BuildAssistExpTable.levelAt(expAmount)

  lazy val levelProgress: Option[BuildLevelProgress] = {
    import cats.implicits._
    Option.when(BuildAssistExpTable.maxLevel > levelCorrespondingToExp) {
      import com.github.unchama.generic.algebra.typeclasses.OrderedMonus._

      val (nextThreshold, previousThreshold) = {
        val nextLevel = levelCorrespondingToExp.incremented

        (
          BuildAssistExpTable.expAt(nextLevel),
          BuildAssistExpTable.expAt(levelCorrespondingToExp)
        )
      }

      val required = nextThreshold |-| previousThreshold
      val achieved = expAmount |-| previousThreshold

      BuildLevelProgress.fromRequiredAndAchievedPair(required, achieved)
    }
  }

  def modifyExpAmount(f: BuildExpAmount => BuildExpAmount): BuildAmountData =
    copy(expAmount = f(expAmount))

  def addExpAmount(another: BuildExpAmount): BuildAmountData =
    modifyExpAmount(_.add(another))

}

object BuildAmountData {

  val initial: BuildAmountData = BuildAmountData(BuildExpAmount(BigDecimal(0)))

  implicit val order: Order[BuildAmountData] = Order.by(_.expAmount)

  implicit val monoid: Monoid[BuildAmountData] =
    Monoid.instance(initial, (a, b) => BuildAmountData(a.expAmount.add(b.expAmount)))
}
