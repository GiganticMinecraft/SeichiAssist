package com.github.unchama.seichiassist.subsystems.breakflags.domain

import enumeratum.{Enum, EnumEntry}

/**
 * 定義されたブロックを破壊するかしないかを表すためのフラグの名前を集めたEnum
 */
sealed trait BreakFlagName extends EnumEntry

object BreakFlagName extends Enum[BreakFlagName] {

  val values: IndexedSeq[BreakFlagName] = findValues

  /**
   * Chestブロックを破壊するかを示すフラグ名
   */
  case object Chest extends BreakFlagName

  /**
   * ネザークォーツ系ブロックを破壊するかを示すフラグ名
   */
  case object NetherQuartz extends BreakFlagName

}
