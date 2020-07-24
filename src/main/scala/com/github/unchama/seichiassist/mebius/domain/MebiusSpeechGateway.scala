package com.github.unchama.seichiassist.mebius.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

/**
 * Mebiusからの発話を仲介するオブジェクトのクラス。
 *
 * 内部状態として発話を許可するかのBooleanを持っており、
 * `tryMakingSpeech` の結果、 `unblockSpeech` するまで
 * `tryMakingSpeech` は副作用を持たない(`Monad[F].unit`と等価)。
 */
class MebiusSpeechGateway[F[_] : Sync](private implicit val effects: MebiusSpeechPresentation[F[Unit]]) {

  private val willBlockSpeech: Ref[F, Boolean] = Ref.unsafe[F, Boolean](false)

  def unblockSpeech(): F[Unit] = willBlockSpeech.set(false)

  /**
   * `property` をプロパティとして持つMebiusに発話させる。
   *
   * 一度このアクションを実行すると、 `unblockSpeech` が行われるまで
   * 次の `tryMakingSpeech` は `Monad[F].unit` と等価になる。
   */
  def tryMakingSpeech(property: MebiusProperty, speech: MebiusSpeech): F[Unit] = {
    import cats.implicits._

    for {
      shouldBlockSpeech <- willBlockSpeech.get
      _ <-
        if (!shouldBlockSpeech) {
          effects.speak(property, speech) >> willBlockSpeech.set(true)
        } else {
          Sync[F].unit
        }
    } yield ()
  }

  /**
   * `property` をプロパティとして持つMebiusに強制的に発話させる。
   */
  def forceMakingSpeech(property: MebiusProperty, speech: MebiusSpeech): F[Unit] = {
    effects.speak(property, speech)
  }

}
