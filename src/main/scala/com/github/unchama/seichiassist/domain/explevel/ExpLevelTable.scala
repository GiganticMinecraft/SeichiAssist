package com.github.unchama.seichiassist.domain.explevel

import com.github.unchama.seichiassist.util.typeclass.HasMinimum

/**
 * 経験値量のテーブル。
 *
 * @param internalTable 経験値の遷移を記述するSeq。
 *                      i番目の要素に、レベルi+1になるのに必要な経験値量が入る。
 *                      この列は単調増加であることが要求される。
 */
class ExpLevelTable[L: Level, ExpAmount: Ordering : HasMinimum](private val internalTable: Seq[ExpAmount]) {

  private val order = implicitly[Ordering[ExpAmount]]

  import order._

  require({
    internalTable.sliding(2).forall { case Seq(x1, x2) =>
      x1 <= x2
    }
  }, "internalTable must be sorted")

  require(internalTable.nonEmpty)

  require({
    internalTable.head == HasMinimum[ExpAmount].minimum
  }, "first element of the table must be the minimum amount")

  def levelAt(expAmount: ExpAmount): L = Level[L].wrapPositive {
    internalTable.lastIndexWhere(_ <= expAmount) + 1
  }

  def maxLevel: L = Level[L].wrapPositive {
    internalTable.size
  }

}
