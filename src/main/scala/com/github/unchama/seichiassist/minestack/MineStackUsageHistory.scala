package com.github.unchama.seichiassist.minestack

class MineStackUsageHistory {

  private val maxListSize = 27

  private var usageHistory: Vector[MineStackObject] = Vector.empty

  /**
   * 履歴に追加します。ただし、データの保存可能な最大値を超えていた場合、先頭から削除されます。
   */
  def addHistory(mineStackObj: MineStackObject): Unit = {
    usageHistory = usageHistory.filterNot(_ == mineStackObj)
    usageHistory :+= mineStackObj
    if (usageHistory.size > maxListSize) usageHistory.drop(1)
  }

  def getHistory: Vector[MineStackObject] = usageHistory

}
