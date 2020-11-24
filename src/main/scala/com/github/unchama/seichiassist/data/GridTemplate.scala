package com.github.unchama.seichiassist.data

import com.github.unchama.seichiassist.util.Util

/**
 * グリッド式保護設定(テンプレート)を保存するためのクラス
 *
 * @author karayuu
 *         2017/9/11
 */
class GridTemplate(var aheadAmount: Int, var behindAmount: Int, var rightAmount: Int, var leftAmount: Int) {
  //ゲッター
  def getAheadAmount: Int = aheadAmount

  def getBehindAmount: Int = behindAmount

  def getRightAmount: Int = rightAmount

  def getLeftAmount: Int = leftAmount

  //セッター
  def setChunkAmount(setMap: Map[Util.DirectionType, Int]): Unit = {
    this.aheadAmount = setMap(Util.DirectionType.ahead)
    this.behindAmount = setMap(Util.DirectionType.behind)
    this.rightAmount = setMap(Util.DirectionType.right)
    this.leftAmount = setMap(Util.DirectionType.left)
  }

  /**
   * 空かどうか
   *
   * @return true: 空 / false: 空でない
   */
  def isEmpty: Boolean = this.aheadAmount == 0 && this.behindAmount == 0 && this.rightAmount == 0 && this.leftAmount == 0

  override def toString: String = s"前方向:${this.aheadAmount},後ろ方向:${this.behindAmount},右方向:${this.rightAmount},左方向:${this.leftAmount}"
}
