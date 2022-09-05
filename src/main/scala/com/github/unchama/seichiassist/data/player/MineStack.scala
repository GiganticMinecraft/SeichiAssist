package com.github.unchama.seichiassist.data.player

import com.github.unchama.seichiassist.subsystems.minestack.domain.MineStackObject
import scala.collection.mutable

class MineStack(_objectCountMap: collection.Map[MineStackObject, Long] = collection.Map.empty) {

  private val objectCountMap = mutable.Map.empty ++ _objectCountMap

  /**
   * 指定されたマインスタックオブジェクトのスタック数を減少させる。
   */
  def subtractStackedAmountOf(mineStackObj: MineStackObject, by: Long): Unit =
    addStackedAmountOf(mineStackObj, -by)

  /**
   * 指定されたマインスタックオブジェクトのスタック数を増加させる。
   */
  def addStackedAmountOf(mineStackObj: MineStackObject, by: Long): Unit =
    setStackedAmountOf(mineStackObj, getStackedAmountOf(mineStackObj) + by)

  def getStackedAmountOf(mineStackObj: MineStackObject): Long =
    objectCountMap.getOrElse(mineStackObj, 0)

  private def setStackedAmountOf(mineStackObj: MineStackObject, to: Long): Unit = {
    objectCountMap.put(mineStackObj, to)
  }

  def getObjectCounts: Map[MineStackObject, Long] = {
    objectCountMap.toMap
  }

}
