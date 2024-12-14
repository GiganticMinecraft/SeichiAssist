package com.github.unchama.seichiassist.subsystems.mana.domain

import cats.kernel.Order

case class ManaAmount(value: Double) {
  require(value >= 0.0, "マナ量は非負である必要があります")

  def add(amount: ManaAmount): ManaAmount = ManaAmount(amount.value + value)

  def tryUse(amount: ManaAmount)(manaMultiplier: ManaMultiplier): Option[ManaAmount] = {
    val resultingAmount = value - amount.multiply(manaMultiplier.value).value
    Option.when(resultingAmount >= 0.0)(ManaAmount(resultingAmount))
  }

  def multiply(rate: Double): ManaAmount = {
    require(rate >= 0.0, "マナ量乗算の倍率は非負である必要があります")
    ManaAmount(rate * value)
  }
}

object ManaAmount {

  implicit val order: Order[ManaAmount] = Order.by(_.value)

}
