package com.github.unchama.seichiassist.minestack

class MineStackUsageHistory {

  private val maxListSize = 27

  private var _usageHistory: Vector[MineStackObject] = Vector.empty

  /**
   * アイテムを履歴に追加します。
   * ただし、追加したことで履歴の最大件数を超えた場合、時系列順で見て最も最初に追加された履歴が削除されます。
   */
  def addHistory(mineStackObject: MineStackObject): Unit = {
    _usageHistory = _usageHistory.filterNot(_ == mineStackObject)
    _usageHistory :+= mineStackObject
    if (_usageHistory.size > maxListSize) {
      _usageHistory = _usageHistory.drop(1)
    }
  }

  def usageHistory: Vector[MineStackObject] = _usageHistory

}
