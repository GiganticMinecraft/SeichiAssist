package com.github.unchama.seichiassist.menus.paging

import scala.math.Integral.Implicits._
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.numeric.GreaterEqual
import eu.timepit.refined.api.Refined

object PageCounter {
  def totalPage(totalItems: Int Refined GreaterEqual[0], itemsPerPage: PosInt): PosInt = {
    if (totalItems.value == 0) return PosInt(1)

    val (basePage, rem) = totalItems.value /% itemsPerPage.value

    val result = if (rem == 0) basePage else basePage + 1

    PosInt.unsafeFrom(result)
  }
}
