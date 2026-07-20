package com.github.unchama.seichiassist.menus.paging

import scala.math.Integral.Implicits._
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.numeric.GreaterEqual
import eu.timepit.refined.api.Refined

object PageCounter {
  private def computeTotalPage(
    totalItems: Int Refined GreaterEqual[0],
    itemsPerPage: PosInt
  ): PosInt = {
    if (totalItems.value == 0) return PosInt.unsafeFrom(1)

    val (basePage, rem) = totalItems.value /% itemsPerPage.value

    val result = if (rem == 0) basePage else basePage + 1

    PosInt.unsafeFrom(result)
  }

  /**
   * ページ数を計算する。`itemsPerPage` はリテラルでなければならず、
   * 正であることはコンパイル時に検証される。
   * Scala 2ではrefined.auto._のマクロが担っていたコンパイル時検証をinline化により置き換えたもの。
   */
  inline def totalPage(
    totalItems: Int Refined GreaterEqual[0],
    inline itemsPerPage: Int
  ): PosInt =
    inline if (itemsPerPage > 0)
      computeTotalPage(totalItems, PosInt.unsafeFrom(itemsPerPage))
    else
      scala.compiletime.error("PageCounter: itemsPerPageは正のリテラルでなければならない")
}
