package com.github.unchama.seichiassist.mebius.domain.speech

import cats.effect.Sync
import cats.effect.concurrent.Ref

import scala.util.Random

/**
 * Mebiusの発話を阻止するかどうかを決定するオブジェクトのクラス。
 */
final class MebiusSpeechBlockageState[F[_] : Sync] {

  private val willBlockSpeech: Ref[F, Boolean] = Ref.unsafe[F, Boolean](false)

  def unblock: F[Unit] = willBlockSpeech.set(false)

  def block: F[Unit] = willBlockSpeech.set(true)

  /**
   * Mebiusの発話を阻止するかどうかを決定する。
   *
   * `unblock` が実行されていれば、25%の確率で、そうでなければ0%の確率で発話する。
   */
  def shouldBlock: F[Boolean] = {
    import cats.implicits._

    for {
      shouldBlockSpeechDueToFlag <- willBlockSpeech.get
      shouldBlockSpeechDueToRandomness <- Sync[F].delay {
        Random.nextDouble() < MebiusSpeechBlockageState.speechBlockProbability
      }
    } yield shouldBlockSpeechDueToFlag || shouldBlockSpeechDueToRandomness
  }

}

object MebiusSpeechBlockageState {
  /**
   * [[MebiusSpeechBlockageState.shouldBlock()]]が、内部状態がfalseの時にも発話を不許可とする確率
   */
  val speechBlockProbability = 0.75
}
