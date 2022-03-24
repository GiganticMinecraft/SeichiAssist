package com.github.unchama.seichiassist.util

import enumeratum.{Enum, EnumEntry}

/**
 * PlayerDataでチャンク数をゲット・セットするためのenum
 */
sealed trait RelativeDirection extends EnumEntry

case object RelativeDirection extends Enum[RelativeDirection] {

  val values: IndexedSeq[RelativeDirection] = findValues

  /**
   * for Java interop
   */
  def ahead: AHEAD.type = AHEAD

  def behind: BEHIND.type = BEHIND

  def right: RIGHT.type = RIGHT

  def left: LEFT.type = LEFT

  case object AHEAD extends RelativeDirection

  case object BEHIND extends RelativeDirection

  case object RIGHT extends RelativeDirection

  case object LEFT extends RelativeDirection
}
