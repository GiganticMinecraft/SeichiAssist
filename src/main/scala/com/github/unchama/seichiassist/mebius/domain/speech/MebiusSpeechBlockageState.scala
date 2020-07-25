package com.github.unchama.seichiassist.mebius.domain.speech

import cats.effect.Sync
import cats.effect.concurrent.Ref

import scala.util.Random

/**
 * Mebiusの発話を阻止するかどうかを決定するオブジェクトのクラス。
 *
 * @param speechBlockProbability ランダムで発話を止める確率
 */
final class MebiusSpeechBlockageState[F[_] : Sync](val speechBlockProbability: Double) {

  private val willBlockSpeech: Ref[F, Boolean] = Ref.unsafe[F, Boolean](false)

  def unblock(): F[Unit] = willBlockSpeech.set(false)

  def block(): F[Unit] = willBlockSpeech.set(true)

  def shouldBlock(): F[Boolean] = {
    import cats.implicits._

    for {
      shouldBlockSpeechDueToFlag <- willBlockSpeech.get
      shouldBlockSpeechDueToRandomness <- Sync[F].delay {
        Random.nextDouble() < speechBlockProbability
      }
    } yield shouldBlockSpeechDueToFlag || shouldBlockSpeechDueToRandomness
  }

}

object MebiusSpeechBlockageState {
  /**
   * [[com.github.unchama.seichiassist.mebius.service.MebiusSpeechService.tryMakingSpeech]]が
   * 発話を不許可とする確率
   */
  val speechBlockProbability = 0.75
}
