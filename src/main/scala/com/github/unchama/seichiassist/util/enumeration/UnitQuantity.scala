package com.github.unchama.seichiassist.util.enumeration

sealed trait UnitQuantity {
  def value: Int
}

object UnitQuantity {
  case object ONE extends UnitQuantity {
    override def value = 1
  }

  case object TEN extends UnitQuantity {
    override def value = 10
  }

  case object ONE_HUNDRED extends UnitQuantity {
    override def value = 100
  }
}
