package com.github.unchama.generic.algebra.typeclasses

import cats.kernel.{CommutativeMonoid, Order}

/**
 * 切り捨て減算(truncated subtraction)を提供する、全順序構造を備える可換モノイド。
 *
 * 以下の法則を満たす。
 *   - `x lteqv y` <=> `x + z = y` となる `z` が存在する。
 *   - `x lteqv (y + z)` => `|-|(x, z) lteqv y`
 */
trait OrderedMonus[A] extends Order[A] with CommutativeMonoid[A] {

  /**
   * 切り捨て減算。 `x: A, y: A` について、 `z: A` = `|-|(x, y)` は `x <= y + z` となるような最小の `z` として定義される。
   */
  def |-|(x: A, y: A): A

  /**
   * 切り捨て減算へのエイリアス。
   */
  def subtractTruncate(a: A, b: A): A = |-|(a, b)

}

object OrderedMonus {

  def apply[A: OrderedMonus]: OrderedMonus[A] = implicitly

  implicit class OrderedMonusOps[A: OrderedMonus](lhs: A) {

    def |-|(y: A): A = OrderedMonus[A].|-|(lhs, y)

  }

}
