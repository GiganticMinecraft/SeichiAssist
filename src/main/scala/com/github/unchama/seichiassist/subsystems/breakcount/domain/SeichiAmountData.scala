package com.github.unchama.seichiassist.subsystems.breakcount.domain

import cats.kernel.Monoid
import cats.{Eq, Order}
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

  /**
   * 整地レベルが最大値でない場合スターレベルの進行度を、最大値である場合スターレベルの進行度を表す値。
   */
  lazy val levelProgress: SeichiLevelProgress = {
    import com.github.unchama.generic.algebra.typeclasses.OrderedMonus._

    val (nextThreshold, previousThreshold) =
      if (starLevelCorrespondingToExp != SeichiStarLevel.zero) {
        val nextLevel = starLevelCorrespondingToExp.increment

        (
          SeichiStarLevelTable.expAt(nextLevel),
          SeichiStarLevelTable.expAt(starLevelCorrespondingToExp)
        )
      } else {
        val nextLevel = levelCorrespondingToExp.increment

        (
          SeichiLevelTable.table.expAt(nextLevel),
          SeichiLevelTable.table.expAt(levelCorrespondingToExp)
        )
      }

    val required = nextThreshold |-| previousThreshold
    val achieved = expAmount |-| previousThreshold

    SeichiLevelProgress.fromRequiredAndAchievedPair(required, achieved)
  }

  def addExpAmount(another: SeichiExpAmount): SeichiAmountData = SeichiAmountData(
    expAmount.add(another)
  )

}

object SeichiAmountData {
  val initial: SeichiAmountData = SeichiAmountData(SeichiExpAmount.zero)

  implicit val eq: Eq[SeichiAmountData] = Eq.by(_.expAmount)

  implicit val order: Order[SeichiAmountData] = Order.by(_.expAmount)

  implicit val monoid: Monoid[SeichiAmountData] =
    Monoid.instance(initial, (a, b) => a.addExpAmount(b.expAmount))
}
