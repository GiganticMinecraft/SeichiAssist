package com.github.unchama.seichiassist.util.typeclass

import simulacrum.typeclass

/**
 * 半順序付けされており最小値が定義されているような型の型クラス
 */
@typeclass trait HasMinimum[T] {
  val ordering: PartialOrdering[T]

  val minimum: T
}

object HasMinimum {
  def as[T](givenMinimum: T)(implicit T: PartialOrdering[T]): HasMinimum[T] = new HasMinimum[T] {
    override val ordering: PartialOrdering[T] = T
    override val minimum: T = givenMinimum
  }
}
