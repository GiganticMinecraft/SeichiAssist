package com.github.unchama.menuinventory

import io.github.iltotore.iron.:|
import io.github.iltotore.iron.constraint.numeric.{GreaterEqual, Interval}

object ChestSlotRef {
  type ChestRowIndexRef = Int :| GreaterEqual[0]
  type ChestColumnIndexRef = Int :| Interval.ClosedOpen[0, 9]

  /**
   * チェストインベントリでのスロットへの参照を計算する
   *
   * リテラルを渡した場合、制約（rowIndexは0以上、columnIndexは0以上9未満）の違反は
   * Ironによりコンパイル時に検出される。実行時に検証済みの値もそのまま渡せる。
   *
   * @param rowIndex
   *   一番上の行から参照したいスロットを含む行まで移動する行数
   * @param columnIndex
   *   一番左の列から参照したいスロットを含む列まで移動する列数
   * @return
   *   `rowIndex`と`columnIndex`により指定されたスロットのスロットid
   */
  def apply(rowIndex: ChestRowIndexRef, columnIndex: ChestColumnIndexRef): Int =
    rowIndex * 9 + columnIndex
}
