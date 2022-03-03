package com.github.unchama.seichiassist.domain.explevel

/**
 * 上限の無いレベル [[Level]] とそれを決定する経験値量 [[ExpAmount]] を相互変換するオブジェクト。
 *
 * `succ` をレベルをインクリメントする操作、 `l: Level`、 `e: ExpAmount` を任意に取るとき、
 *
 *   - `levelAt(expAt(l)) = l`
 *   - `expAt(levelAt(e)) <= e < expAt(succ(levelAt(e)))`
 *
 * を満たす。
 */
trait ExpLevelConversion[Level, ExpAmount] {

  /**
   * 与えられた経験値量 `expAmount` で到達できるレベルを計算する。
   */
  def levelAt(expAmount: ExpAmount): Level

  /**
   * 与えられたレベル `level` に上がるのに必要な経験値量を計算する。
   */
  def expAt(level: Level): ExpAmount

}
