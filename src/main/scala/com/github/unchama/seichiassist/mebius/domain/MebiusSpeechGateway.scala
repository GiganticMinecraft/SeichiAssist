package com.github.unchama.seichiassist.mebius.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

import scala.util.Random

/**
 * Mebiusからの発話を仲介するオブジェクトのクラス。
 *
 * 内部状態として発話を許可するかのBooleanを持っており、
 * `tryMakingSpeech` の結果、 `unblockSpeech` するまで
 * `tryMakingSpeech` は副作用を持たない(`Monad[F].unit`と等価)。
 */
abstract class MebiusSpeechGateway[F[_] : Sync] {

  private val willBlockSpeech: Ref[F, Boolean] = Ref.unsafe[F, Boolean](false)

  final def unblockSpeech(): F[Unit] = willBlockSpeech.set(false)

  /**
   * `property` をプロパティとして持つMebiusに発話させる。
   *
   * 一度このアクションにてMebiusが発話された場合、 `unblockSpeech` が行われるまで
   * 次の `tryMakingSpeech` は `Monad[F].unit` と等価になる。
   *
   * また、 `unblockSpeech` が事前に呼ばれていたとしても、50%の確率で発話は行われない。
   */
  def tryMakingSpeech(property: MebiusProperty, speech: MebiusSpeech): F[Unit] = {
    import cats.implicits._

    for {
      shouldBlockSpeechDueToFlag <- willBlockSpeech.get
      shouldBlockSpeechDueToRandomness <- Sync[F].delay(Random.nextBoolean())
      _ <-
        if (!shouldBlockSpeechDueToFlag && !shouldBlockSpeechDueToRandomness) {
          speak(property, speech) >> willBlockSpeech.set(true)
        } else {
          Sync[F].unit
        }
    } yield ()
  }

  private def speak(speakingMebiusProperty: MebiusProperty, speech: MebiusSpeech): F[Unit] = {
    import cats.implicits._

    sendMessage(speakingMebiusProperty, speech.content) >> playSpeechSound(speech.strength)
  }

  /**
   * `property` をプロパティとして持つMebiusに強制的に発話させる。
   */
  def forceMakingSpeech(property: MebiusProperty, speech: MebiusSpeech): F[Unit] = {
    speak(property, speech)
  }

  protected def sendMessage(property: MebiusProperty, message: String): F[Unit]

  protected def playSpeechSound(strength: MebiusSpeechStrength): F[Unit]

}
