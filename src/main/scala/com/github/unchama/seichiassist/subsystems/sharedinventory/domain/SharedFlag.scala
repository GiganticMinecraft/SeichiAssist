package com.github.unchama.seichiassist.subsystems.sharedinventory.domain

import enumeratum._

sealed trait SharedFlag extends EnumEntry

object SharedFlag extends Enum[SharedFlag] {

  case object Sharing extends SharedFlag
  case object NotSharing extends SharedFlag

  override def values: IndexedSeq[SharedFlag] = findValues
}
