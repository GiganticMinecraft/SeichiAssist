package com.github.unchama.seichiassist.domain.explevel

import com.github.unchama.seichiassist.util.typeclass.HasMinimum

import scala.collection.Searching

/**
 * 経験値量のテーブル。
 *
 * @param internalTable 経験値の遷移を記述するSeq。
 *                      i番目の要素に、レベルi+1になるのに必要な経験値量が入る。
 *                      この列は単調増加であることが要求される。
 */
class ExpLevelTable[L: Level, ExpAmount: Ordering : HasMinimum](private val internalTable: IndexedSeq[ExpAmount]) {

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
    internalTable.search(expAmount) match {
      case Searching.Found(foundIndex) => foundIndex + 1
      case Searching.InsertionPoint(insertionPoint) => insertionPoint
    }
  }

  def maxLevel: L = Level[L].wrapPositive {
    internalTable.size
  }

}
