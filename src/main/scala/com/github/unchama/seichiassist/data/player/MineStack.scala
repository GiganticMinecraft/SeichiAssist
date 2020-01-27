package com.github.unchama.seichiassist.data.player

import com.github.unchama.seichiassist.minestack.MineStackObj

import scala.collection.immutable
import cats.effect.concurrent.Ref

class MineStack(_objectCountMap: collection.Map[MineStackObj, Long] = collection.Map.empty) {

  private val objectCountMap = Ref.unsafe(immutable.Map.empty ++ _objectCountMap)

  /**
   * 指定されたマインスタックオブジェクトのスタック数を減少させる。
   */
  def subtractStackedAmountOf(mineStackObj: MineStackObj, by: Long): Unit =
    addStackedAmountOf(mineStackObj, -by)

  /**
   * 指定されたマインスタックオブジェクトのスタック数を増加させる。
   */
  def addStackedAmountOf(mineStackObj: MineStackObj, by: Long): Unit =
    setStackedAmountOf(mineStackObj, getStackedAmountOf(mineStackObj) + by)

  def getStackedAmountOf(mineStackObj: MineStackObj): Long = objectCountMap.get().getOrElse(mineStackObj, 0)

  private def setStackedAmountOf(mineStackObj: MineStackObj, to: Long): Unit = {
    objectCountMap.set(objectCountMap.get() + (mineStackObj, to))
  }
}
