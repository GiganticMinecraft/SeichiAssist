package com.github.unchama.generic.algebra.typeclasses

import cats.Order

/**
 * 正のIntと同型な型のクラス。
 *
 * すべての正のnについて、
 *
 *   - `wrapPositive(n).asInt = n`
 *
 * を満たす。
 */
trait PositiveInt[T] {

  /**
   * 正整数を包む。非正整数については例外を投げる。
   */
  def wrapPositive(int: Int): T

  /**
   * [[T]] を正整数に変換する。
   */
  def asInt(t: T): Int

}

object PositiveInt {

  def apply[T](implicit instance: PositiveInt[T]): PositiveInt[T] = instance

  implicit def positiveIntHasOrder[T: PositiveInt]: Order[T] = Order.by(PositiveInt[T].asInt)

}
