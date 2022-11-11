package com.github.unchama.generic

import scala.annotation.tailrec

object MathExtra {

  /**
   * 漸化式のように初期値から最終値までを計算する関数
   * `order`の順で計算し、`initial`から計算した順序で返す
   * @param order 計算順序
   * @param initial 初期値
   * @param computedValues 計算済みの値(初期値は`initial`が代入された[[List]])
   * @return 計算結果
   */
  @tailrec
  def recurrenceRelation[A](order: Map[A, A], initial: A)(
    computedValues: List[A] = List(initial)
  ): List[A] = {
    val isOrderExists = order.exists(_._1 == initial)
    val _computedValues = if (isOrderExists) {
      order(initial) +: computedValues
    } else {
      computedValues
    }

    val isWentAround = computedValues.last == initial
    if (!isOrderExists || isWentAround) computedValues.reverse
    else recurrenceRelation(order, order(initial))(_computedValues)
  }

}
