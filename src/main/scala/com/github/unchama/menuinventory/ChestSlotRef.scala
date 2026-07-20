package com.github.unchama.menuinventory

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.{Interval, NonNegative}

object ChestSlotRef {
  type ChestRowIndexRef = Int Refined NonNegative
  type ChestColumnIndexRef = Int Refined Interval.ClosedOpen[0, 9]

  /**
   * チェストインベントリでのスロットへの参照を計算する。
   *
   * 引数はリテラルでなければならず、制約（rowIndexは0以上、columnIndexは0以上9未満）の違反は
   * コンパイル時に検出される。Scala 2ではrefined.auto._のマクロが担っていたコンパイル時検証を
   * inline化により置き換えたもの。実行時に検証済みの値から計算する場合は[[fromRefined]]を使う。
   *
   * @param rowIndex
   *   一番上の行から参照したいスロットを含む行まで移動する行数
   * @param columnIndex
   *   一番左の列から参照したいスロットを含む列まで移動する列数
   * @return
   *   `rowIndex`と`columnIndex`により指定されたスロットのスロットid
   */
  inline def apply(inline rowIndex: Int, inline columnIndex: Int): Int =
    inline if (rowIndex >= 0 && columnIndex >= 0 && columnIndex < 9)
      rowIndex * 9 + columnIndex
    else
      scala.compiletime.error("ChestSlotRef: rowIndexは0以上、columnIndexは0以上9未満のリテラルでなければならない")

  /**
   * 実行時に検証済みのrefined値からスロットidを計算する。
   */
  def fromRefined(rowIndex: ChestRowIndexRef, columnIndex: ChestColumnIndexRef): Int =
    rowIndex.value * 9 + columnIndex.value
}
