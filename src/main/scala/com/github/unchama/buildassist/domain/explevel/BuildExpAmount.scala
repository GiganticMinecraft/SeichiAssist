package com.github.unchama.buildassist.domain.explevel

import com.github.unchama.generic.algebra.instances.BigDecimalIsAdditiveTotallyOrderedGroup
import com.github.unchama.generic.algebra.typeclasses.TotallyOrderedGroup
import com.github.unchama.seichiassist.util.typeclass.HasMinimum

case class BuildExpAmount(amount: BigDecimal) extends AnyVal {

  def mapAmount(f: BigDecimal => BigDecimal): BuildExpAmount = BuildExpAmount(f(amount))

  def add(a: BuildExpAmount): BuildExpAmount = mapAmount(_ + a.amount)

  def toPlainString: String = amount.setScale(1, BigDecimal.RoundingMode.HALF_UP).bigDecimal.toPlainString

}

private[explevel] abstract class BuildExpAmountInstances {
  implicit lazy val ordering: Ordering[BuildExpAmount] = Ordering.by(_.amount)

  implicit lazy val hasMinimum: HasMinimum[BuildExpAmount] = {
    HasMinimum.as(BuildExpAmount(0))
  }

  implicit lazy val totallyOrderedGroup: TotallyOrderedGroup[BuildExpAmount] = {
    val BigDecimal = BigDecimalIsAdditiveTotallyOrderedGroup
    new TotallyOrderedGroup[BuildExpAmount] {
      override def compare(x: BuildExpAmount, y: BuildExpAmount): Int =
        BigDecimal.compare(x.amount, y.amount)

      override def inverse(a: BuildExpAmount): BuildExpAmount =
        a.mapAmount(BigDecimal.inverse)

      override val empty: BuildExpAmount =
        BuildExpAmount(0)

      override def combine(x: BuildExpAmount, y: BuildExpAmount): BuildExpAmount =
        x.mapAmount(BigDecimal.combine(_, y.amount))
    }
  }
}

object BuildExpAmount extends BuildExpAmountInstances {
  def apply(amount: Int): BuildExpAmount = BuildExpAmount(BigDecimal(amount))
}
