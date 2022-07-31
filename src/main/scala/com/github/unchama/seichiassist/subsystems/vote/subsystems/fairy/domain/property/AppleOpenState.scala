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

  /**
   * ガンガン食べるぞ
   */
  case object Permissible extends AppleOpenState(1)

  /**
   * バッチリたべよう
   */
  case object Consume extends AppleOpenState(2)

  /**
   * リンゴだいじに
   */
  case object LessConsume extends AppleOpenState(3)

  /**
   * リンゴつかうな
   */
  case object NoConsume extends AppleOpenState(4)

}
