package com.github.unchama.seichiassist.subsystems.buildcount.domain

import com.github.unchama.generic.algebra.typeclasses.OrderedMonus
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount

case class BuildAmountPermission(raw: BuildExpAmount)

object BuildAmountPermission {
  implicit val orderedMonus: OrderedMonus[BuildAmountPermission] = new OrderedMonus[BuildAmountPermission] {
    private val expIsOrderedMonus = OrderedMonus[BuildExpAmount]
    /**
     * 切り捨て減算。
     * `x: A, y: A` について、 `z: A` = `|-|(x, y)` は
     * `x <= y + z` となるような最小の `z` として定義される。
     */
    override def |-|(x: BuildAmountPermission, y: BuildAmountPermission): BuildAmountPermission = BuildAmountPermission(expIsOrderedMonus.|-|(x.raw, y.raw))

    override def empty: BuildAmountPermission = BuildAmountPermission(expIsOrderedMonus.empty)

    override def combine(x: BuildAmountPermission, y: BuildAmountPermission): BuildAmountPermission = BuildAmountPermission(expIsOrderedMonus.combine(x.raw, y.raw))

    override def compare(x: BuildAmountPermission, y: BuildAmountPermission): Int = expIsOrderedMonus.compare(x.raw, y.raw)
  }
}