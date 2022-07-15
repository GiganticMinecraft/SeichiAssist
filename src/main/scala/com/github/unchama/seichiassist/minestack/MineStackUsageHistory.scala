package com.github.unchama.seichiassist.minestack

class MineStackUsageHistory {

  private val maxListSize = 27

  private var _usageHistory: Vector[MineStackObject] = Vector.empty

  /**
   * 履歴に追加します。ただし、データの保存可能な最大値を超えていた場合、先頭から削除されます。
   */
  def addHistory(mineStackObjectGroup: MineStackObject): Unit = {
    _usageHistory = _usageHistory.filterNot(_ == mineStackObjectGroup)
    _usageHistory :+= mineStackObjectGroup
    if (_usageHistory.size > maxListSize) _usageHistory.drop(1)
  }

  def usageHistory: Vector[MineStackObject] = _usageHistory

}
