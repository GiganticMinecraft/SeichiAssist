package com.github.unchama.seichiassist.domain.explevel

import cats.kernel.{LowerBounded, Monoid, Order}
import com.github.unchama.generic.algebra.typeclasses.PositiveInt

import scala.collection.Searching

/**
 * 経験値量とレベルを相互変換する有限のテーブル。 [[ExpLevelConversion]] と似ているが、こちらはレベルに有限性を仮定していることに注意。
 *
 * @param internalTable
 *   経験値の遷移を記述するSeq。 i番目の要素に、レベルi+1になるのに必要な経験値量が入る。 この列は単調増加であることが要求される。
 */
class FiniteExpLevelTable[L: PositiveInt, ExpAmount: Order: LowerBounded](
  private val internalTable: Vector[ExpAmount]
) {

  import cats.implicits._

  require(
    {
      internalTable.sliding(2).forall {
        case Seq(x1, x2) =>
          x1 <= x2
      }
    },
    "internalTable must be sorted"
  )

  require(internalTable.nonEmpty, "table must be nonempty")

  require(
    {
      internalTable.head == LowerBounded[ExpAmount].minBound
    },
    "first element of the table must be the minimum amount"
  )

  /**
   * 与えられた経験値量 `expAmount` で到達できるレベルを計算する。
   */
  def levelAt(expAmount: ExpAmount): L = PositiveInt[L].wrapPositive {
    internalTable.search(expAmount) match {
      case Searching.Found(foundIndex)              => foundIndex + 1
      case Searching.InsertionPoint(insertionPoint) => insertionPoint
    }
  }

  /**
   * 与えられたレベル `level` に上がるのに必要な経験値量を計算する。 `level` が `maxLevel` よりも真に大きい場合、 `maxLevel`
   * に到達するのに必要な経験値量が返される。
   */
  def expAt(level: L): ExpAmount = {
    val rawLevel = PositiveInt[L].asInt(level)

    if (rawLevel > internalTable.size) expAt(maxLevel)
    else internalTable(rawLevel - 1)
  }

  /**
   * このテーブルが定義する最大のレベル。
   */
  def maxLevel: L = PositiveInt[L].wrapPositive {
    internalTable.size
  }

  /**
   * このテーブルが定義するレベルの範囲。
   */
  def levelRange: Seq[L] = (1 to internalTable.size).map(PositiveInt[L].wrapPositive)

  /**
   * このテーブルを与えられたレベルまで延長するためのビルダーを返す。
   */
  def extendToLevel(level: L): ExtensionBuilder = ExtensionBuilder(level)

  case class ExtensionBuilder(extensionTarget: L) {

    /**
     * [[extensionTarget]] まで、レベルを1延長するごとに必要な経験値量を `exp` 増やすよう延長したテーブルを返す。
     */
    def withLinearIncreaseOf(
      exp: ExpAmount
    )(implicit addition: Monoid[ExpAmount]): FiniteExpLevelTable[L, ExpAmount] = {
      val lengthToFill =
        (PositiveInt[L].asInt(extensionTarget) - PositiveInt[L].asInt(maxLevel)) max 0
      val lastThreshold = internalTable.last
      val extensionHead = addition.combine(lastThreshold, exp)
      val extension = Vector.iterate(extensionHead, lengthToFill)(addition.combine(_, exp))

      new FiniteExpLevelTable(internalTable.appendedAll(extension))
    }
  }

}
