package com.github.unchama.seichiassist.subsystems.minestack.domain

import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObject

class MineStackUsageHistory[ItemStack] {

  private val maxListSize = 27

  private var _usageHistory: Vector[MineStackObject[ItemStack]] = Vector.empty

  /**
   * 履歴に追加します。ただし、データの保存可能な最大値を超えていた場合、先頭から削除されます。
   */
  def addHistory(mineStackObject: MineStackObject[ItemStack]): Unit = {
    _usageHistory = _usageHistory.filterNot(_ == mineStackObject)
    _usageHistory :+= mineStackObject
    if (_usageHistory.size > maxListSize) _usageHistory = _usageHistory.drop(1)
  }

  def usageHistory: Vector[MineStackObject[ItemStack]] = _usageHistory

}
