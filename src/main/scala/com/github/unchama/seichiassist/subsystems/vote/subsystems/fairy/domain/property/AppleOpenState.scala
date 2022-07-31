package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

import enumeratum._

/**
 * がちゃりんごの開放状況を管理するクラス
 * @param serializedValue
 *  開放状況を永続化する際に必要な番号
 */
sealed class AppleOpenState(val serializedValue: Int) extends EnumEntry

case object AppleOpenState extends Enum[AppleOpenState] {

  override val values: IndexedSeq[AppleOpenState] = findValues

  case object OpenAnyway extends AppleOpenState(1)

  case object Open extends AppleOpenState(2)

  case object OpenALittle extends AppleOpenState(3)

  case object NotOpen extends AppleOpenState(4)

}
