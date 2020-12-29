package com.github.unchama.seichiassist.util.enumeration

import enumeratum.{Enum, EnumEntry}

/**
 * PlayerDataでチャンク数をゲット・セットするためのenum
 */
sealed trait DirectionType extends EnumEntry

case object DirectionType extends Enum[DirectionType] {

  val values: IndexedSeq[DirectionType] = findValues

  /**
   * for Java interop
   */
  def ahead: AHEAD.type = AHEAD

  def behind: BEHIND.type = BEHIND

  def right: RIGHT.type = RIGHT

  def left: LEFT.type = LEFT

  case object AHEAD extends DirectionType

  case object BEHIND extends DirectionType

  case object RIGHT extends DirectionType

  case object LEFT extends DirectionType
}