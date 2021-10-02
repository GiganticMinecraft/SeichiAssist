package com.github.unchama.seichiassist.subsystems.chatratelimiter.domain

import cats.Order
import com.github.unchama.generic.algebra.typeclasses.OrderedMonus

case class ChatCount(count: Long) extends AnyVal

object ChatCount {

  implicit val orderedMonus: OrderedMonus[ChatCount] = new OrderedMonus[ChatCount] {
    /**
     * 切り捨て減算。
     * `x: A, y: A` について、 `z: A` = `|-|(x, y)` は
     * `x <= y + z` となるような最小の `z` として定義される。
     */
    override def |-|(x: ChatCount, y: ChatCount): ChatCount = ChatCount((x.count - y.count) min 0L)

    override def compare(x: ChatCount, y: ChatCount): Int = Order.by((a: ChatCount) => a.count).compare(x, y)

    override def empty: ChatCount = ChatCount(0)

    override def combine(x: ChatCount, y: ChatCount): ChatCount = ChatCount(x.count + y.count)
  }
}
