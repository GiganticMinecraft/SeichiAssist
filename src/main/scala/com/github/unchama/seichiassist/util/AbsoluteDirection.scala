package com.github.unchama.seichiassist.util

import enumeratum.{Enum, EnumEntry}

/**
 * PlayerDataなどで使用する方角関係のenum
 */
sealed trait AbsoluteDirection extends EnumEntry

case object AbsoluteDirection extends Enum[AbsoluteDirection] {

  val values: IndexedSeq[AbsoluteDirection] = findValues

  case object NORTH extends AbsoluteDirection

  case object SOUTH extends AbsoluteDirection

  case object EAST extends AbsoluteDirection

  case object WEST extends AbsoluteDirection
}
