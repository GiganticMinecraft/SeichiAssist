package com.github.unchama.seichiassist.menus.paging

import io.github.iltotore.iron.constraint.numeric.{GreaterEqual, Positive}
import io.github.iltotore.iron.{:|, refineUnsafe}

import scala.math.Integral.Implicits._

object PageCounter {

  /**
   * ページ数を計算する。`itemsPerPage` にリテラルを渡した場合、正であることは
   * Ironによりコンパイル時に検証される。
   */
  def totalPage(totalItems: Int :| GreaterEqual[0], itemsPerPage: Int :| Positive): Int :|
    Positive = {
    if (totalItems == 0) return 1.refineUnsafe

    val (basePage, rem) = (totalItems: Int) /% itemsPerPage

    val result = if (rem == 0) basePage else basePage + 1

    result.refineUnsafe
  }
}
