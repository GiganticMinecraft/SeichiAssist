package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

import enumeratum._

import scala.concurrent.duration.FiniteDuration

/**
 * がちゃりんごの消費戦略を管理するクラス
 * @param serializedValue
 *  消費戦略を永続化する際に必要な番号であり、その値は戦略ごとに異なっていなければならない。
 */
sealed abstract class FairyAppleConsumeStrategy(val serializedValue: Int) extends EnumEntry {
  def isRecoveryTiming(consumptionPeriod: FiniteDuration): Boolean
}

case object FairyAppleConsumeStrategy extends Enum[FairyAppleConsumeStrategy] {

  override val values: IndexedSeq[FairyAppleConsumeStrategy] = findValues

  /**
   * ガンガン食べるぞ
   * 30秒ごとにマナを回復します。
   */
  case object Permissible extends FairyAppleConsumeStrategy(1) {
    override def isRecoveryTiming(consumptionPeriod: FiniteDuration): Boolean = true
  }

  /**
   * バッチリたべよう
   * 1分ごとにマナを回復します。
   */
  case object Consume extends FairyAppleConsumeStrategy(2) {
    override def isRecoveryTiming(consumptionPeriod: FiniteDuration): Boolean =
      consumptionPeriod.toSeconds % 60 == 0
  }

  /**
   * リンゴだいじに
   * 1分30秒ごとにマナを回復します。
   */
  case object LessConsume extends FairyAppleConsumeStrategy(3) {
    override def isRecoveryTiming(consumptionPeriod: FiniteDuration): Boolean =
      consumptionPeriod.toSeconds % 90 == 0
  }

  /**
   * リンゴつかうな
   * 2分ごとにマナを回復します。
   */
  case object NoConsume extends FairyAppleConsumeStrategy(4) {
    override def isRecoveryTiming(consumptionPeriod: FiniteDuration): Boolean =
      consumptionPeriod.toSeconds % 120 == 0
  }

}
