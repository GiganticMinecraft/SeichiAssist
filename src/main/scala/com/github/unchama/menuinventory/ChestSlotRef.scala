package com.github.unchama.menuinventory

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.{Interval, NonNegative}

object ChestSlotRef {
  type ChestRowIndexRef = Int Refined NonNegative
  type ChestColumnIndexRef = Int Refined Interval.ClosedOpen[0, 9]

  /**
   * チェストインベントリでのスロットへの参照を計算する
   * @param rowIndex 一番上の行から参照したいスロットを含む行まで移動する行数
   * @param columnIndex 一番左の列から参照したいスロットを含む列まで移動する列数
   * @return `rowIndex`と`columnIndex`により指定されたスロットのスロットid
   */
  def apply(rowIndex: ChestRowIndexRef, columnIndex: ChestColumnIndexRef): Int = rowIndex.value * 9 + columnIndex.value
}
