package com.github.unchama.generic.algebra.typeclasses

import cats.Order
import simulacrum.typeclass

/**
 * 後者関数 `successor` を兼ね揃えた全順序集合の型クラス。
 *
 * この型クラスのインスタンスは、任意の `x: T` と `y: T` について
 *   - `x < y` ならば `x < x.successor <= y` を満たす。
 */
@typeclass trait HasSuccessor[T] extends AnyRef {

  import cats.implicits._

  /**
   * [[T]] の順序。
   */
  implicit val order: Order[T]

  /**
   * 与えられた値の「次」の値を計算する部分関数。
   */
  def successor(x: T): Option[T]

  /**
   * `lower` 以上 `upper` 以下の要素の順序付けられた [[LazyList]] を作成する。
   *
   * 返される [[LazyList]] を `l` とし、 `0 <= i < (l.size - 1)`、 `0 <= j < l.size` であるとき、
   *   - `i == 0` ならば、 `l(i) = lower`
   *   - `successor(l(i)) = l(i + 1)`
   *   - `lower <= l(j) <= upper` を満たす。
   *
   * この関数が返す [[LazyList]] は、 必ずしもすべての `lower <= x < upper` となる要素を取りつくすわけではない。
   * 具体的には、自然数の二つ組の辞書式順序において `(1, 0)` はいかなる要素の後者でもないから、 `range((0, 0), (2, 0))` は (`(0, 0) < (1,
   * 0) < (2, 0)` であるにもかかわらず) `(1, 0)` を含まない。
   */
  final def closedRange(lower: T, upper: T): LazyList[T] = {
    if (lower > upper) {
      LazyList.empty
    } else {
      LazyList(lower).combine {
        LazyList.unfold(lower) { current =>
          successor(current).filter(_ <= upper).map(t => (t, t))
        }
      }
    }
  }

  final def leftOpenRightClosedRange(lower: T, upper: T): LazyList[T] =
    closedRange(lower, upper).drop(1)

}

object HasSuccessor {

  implicit def positiveIntHasSuccessor[T: PositiveInt]: HasSuccessor[T] =
    new HasSuccessor[T] {
      override implicit val order: Order[T] = PositiveInt.positiveIntHasOrder[T]

      override def successor(x: T): Option[T] = Some {
        PositiveInt[T].wrapPositive(PositiveInt[T].asInt(x) + 1)
      }
    }

}
