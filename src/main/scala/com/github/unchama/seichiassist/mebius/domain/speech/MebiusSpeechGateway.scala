package com.github.unchama.seichiassist.mebius.domain.speech

import cats.effect.Sync
import com.github.unchama.seichiassist.mebius.domain.property.MebiusProperty

/**
 * Mebiusからの発話を仲介するオブジェクトのクラス。
 *
 * 内部状態として発話を許可するかの[[MebiusSpeechBlockageState]]を持っており、
 * `tryMakingSpeech` の結果、 `blockageState.unblock()` するまで
 * `tryMakingSpeech` は副作用を持たない(`Monad[F].unit`と等価)。
 */
abstract class MebiusSpeechGateway[F[_] : Sync] {

  val blockageState = new MebiusSpeechBlockageState[F](MebiusSpeechGateway.speechBlockProbability)

  /**
   * `property` をプロパティとして持つMebiusに発話させる。
   *
   * 一度このアクションにてMebiusが発話された場合、 `unblockSpeech` が行われるまで
   * 次の `tryMakingSpeech` は `Monad[F].unit` と等価になる。
   *
   * また、 `unblockSpeech` が事前に呼ばれていたとしても、[[MebiusSpeechGateway.speechBlockProbability]]の確率で発話は行われない。
   */
  def tryMakingSpeech(property: MebiusProperty, speech: MebiusSpeech): F[Unit] = {
    import cats.implicits._

    for {
      block <- blockageState.shouldBlock()
      _ <-
        if (!block) makeSpeechIgnoringBlockage(property, speech) >> blockageState.block()
        else Sync[F].unit
    } yield ()
  }

  /**
   * `property` をプロパティとして持つMebiusに強制的に発話させる。
   */
  def makeSpeechIgnoringBlockage(property: MebiusProperty, speech: MebiusSpeech): F[Unit] = {
    import cats.implicits._

    sendMessage(property, speech.content) >> playSpeechSound(speech.strength)
  }

  protected def sendMessage(property: MebiusProperty, message: String): F[Unit]

  protected def playSpeechSound(strength: MebiusSpeechStrength): F[Unit]

}

object MebiusSpeechGateway {
  /**
   * [[MebiusSpeechGateway.tryMakingSpeech]]が発話を不許可とする確率
   */
  val speechBlockProbability = 0.75
}
