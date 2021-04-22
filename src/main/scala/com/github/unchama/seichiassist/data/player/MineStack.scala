package com.github.unchama.seichiassist.data.player

import com.github.unchama.seichiassist.minestack.MineStackObj

import scala.collection.mutable

class MineStack(_objectCountMap: collection.Map[MineStackObj[_], Long] = collection.Map.empty) {

  private val objectCountMap = mutable.Map.empty ++ _objectCountMap

  /**
   * 指定されたマインスタックオブジェクトのスタック数を減少させる。
   */
  def subtractStackedAmountOf(mineStackObj: MineStackObj[_], by: Long): Unit =
    addStackedAmountOf(mineStackObj, -by)

  /**
   * 指定されたマインスタックオブジェクトのスタック数を増加させる。
   */
  def addStackedAmountOf(mineStackObj: MineStackObj[_], by: Long): Unit =
    setStackedAmountOf(mineStackObj, getStackedAmountOf(mineStackObj) + by)

  def getStackedAmountOf(mineStackObj: MineStackObj[_]): Long = objectCountMap.getOrElse(mineStackObj, 0)

  private def setStackedAmountOf(mineStackObj: MineStackObj[_], to: Long): Unit = {
    objectCountMap.put(mineStackObj, to)
  }

}
