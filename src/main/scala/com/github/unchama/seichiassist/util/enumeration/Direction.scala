package com.github.unchama.seichiassist.util.enumeration

import enumeratum.{Enum, EnumEntry}

/**
 * PlayerDataなどで使用する方角関係のenum
 */
sealed trait Direction extends EnumEntry

case object Direction extends Enum[Direction] {

  val values: IndexedSeq[Direction] = findValues

  case object NORTH extends Direction

  case object SOUTH extends Direction

  case object EAST extends Direction

  case object WEST extends Direction
}