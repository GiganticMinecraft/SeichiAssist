package com.github.unchama.seichiassist.data.player

import com.github.unchama.seichiassist.minestack.MineStackObj
import java.util.*

class MineStack(objectCountMap: Map<MineStackObj, Long>) {

  private val objectCountMap = HashMap(objectCountMap)

  constructor() : this(HashMap())

  fun getStackedAmountOf(mineStackObj: MineStackObj): Long = objectCountMap[mineStackObj] ?: 0

  private fun setStackedAmountOf(mineStackObj: MineStackObj, to: Long) {
    objectCountMap[mineStackObj] = to
  }

  /**
   * 指定されたマインスタックオブジェクトのスタック数を増加させる。
   */
  fun addStackedAmountOf(mineStackObj: MineStackObj, by: Long) =
      setStackedAmountOf(mineStackObj, getStackedAmountOf(mineStackObj) + by)

  /**
   * 指定されたマインスタックオブジェクトのスタック数を減少させる。
   */
  fun subtractStackedAmountOf(mineStackObj: MineStackObj, by: Long) =
      addStackedAmountOf(mineStackObj, -by)

}
