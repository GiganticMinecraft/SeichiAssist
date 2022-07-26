package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

import enumeratum._

sealed class AppleOpenState(val amount: Int) extends EnumEntry

case object AppleOpenState extends Enum[AppleOpenState] {

  override val values: IndexedSeq[AppleOpenState] = findValues

  case object OpenAnyway extends AppleOpenState(4)

  case object Open extends AppleOpenState(3)

  case object OpenALittle extends AppleOpenState(2)

  case object NotOpen extends AppleOpenState(1)

}
