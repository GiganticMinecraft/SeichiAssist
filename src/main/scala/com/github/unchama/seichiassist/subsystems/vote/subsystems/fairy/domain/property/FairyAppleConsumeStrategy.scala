package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

import enumeratum._

/**
 * がちゃりんごの消費戦略を管理するクラス
 * @param serializedValue
 *  消費戦略を永続化する際に必要な番号であり、その値は戦略ごとに異なっていなければならない。
 */
sealed class FairyAppleConsumeStrategy(val serializedValue: Int) extends EnumEntry

case object FairyAppleConsumeStrategy extends Enum[FairyAppleConsumeStrategy] {

  override val values: IndexedSeq[FairyAppleConsumeStrategy] = findValues

  /**
   * ガンガン食べるぞ
   * 30秒ごとにマナを回復します。
   */
  case object Permissible extends FairyAppleConsumeStrategy(1)

  /**
   * バッチリたべよう
   * 1分ごとにマナを回復します。
   */
  case object Consume extends FairyAppleConsumeStrategy(2)

  /**
   * リンゴだいじに
   * 1分30秒ごとにマナを回復します。
   */
  case object LessConsume extends FairyAppleConsumeStrategy(3)

  /**
   * リンゴつかうな
   * 2分ごとにマナを回復します。
   */
  case object NoConsume extends FairyAppleConsumeStrategy(4)

}
