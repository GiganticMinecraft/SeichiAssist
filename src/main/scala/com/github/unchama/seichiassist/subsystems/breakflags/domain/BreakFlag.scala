package com.github.unchama.seichiassist.subsystems.breakflags.domain

/**
 * 定義されたブロックを破壊するかしないかを表すためのFlagのEnum
 */
sealed trait BreakFlag

object BreakFlag {

  /**
   * Chestブロックを破壊するかを示すEnum
   */
  case object Chest extends BreakFlag

  /**
   * ネザークォーツ系ブロックを破壊するかを示すEnum
   */
  case object NetherQuartz extends BreakFlag

}
