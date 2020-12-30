package com.github.unchama.seichiassist.data

/**
 * グリッド式保護設定(テンプレート)を保存するためのクラス
 *
 * @author karayuu
 *         2017/9/11
 */
case class GridTemplate(aheadAmount: Int, behindAmount: Int, rightAmount: Int, leftAmount: Int) {
  /**
   * 空かどうか
   *
   * @return true: 空 / false: 空でない
   */
  def isEmpty: Boolean = aheadAmount == 0 && behindAmount == 0 && rightAmount == 0 && leftAmount == 0

  override def toString: String = s"前方向:$aheadAmount,後ろ方向:$behindAmount,右方向:$rightAmount,左方向:$leftAmount"
}
