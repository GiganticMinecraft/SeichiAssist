package com.github.unchama.seichiassist.subsystems.chatratelimiter.domain

import com.github.unchama.generic.algebra.typeclasses.OrderedMonus

sealed trait ChatCount

object ChatCount {
  case object Zero extends ChatCount
  case object One extends ChatCount
  implicit val orderedMonus: OrderedMonus[ChatCount] = new OrderedMonus[ChatCount] {

    /**
     * 切り捨て減算。 `x: A, y: A` について、 `z: A` = `|-|(x, y)` は `x <= y + z` となるような最小の `z` として定義される。
     */
    override def |-|(x: ChatCount, y: ChatCount): ChatCount = (x, y) match {
      case (One, Zero) => One
      case _           => Zero
    }

    override def compare(x: ChatCount, y: ChatCount): Int =
      if (x == y) 0 else if (x == One) 1 else -1

    override def empty: ChatCount = Zero

    override def combine(x: ChatCount, y: ChatCount): ChatCount = (x, y) match {
      case (Zero, Zero) => Zero
      case _            => One
    }
  }
}
