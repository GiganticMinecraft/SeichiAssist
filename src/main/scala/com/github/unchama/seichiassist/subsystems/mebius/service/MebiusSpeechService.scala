package com.github.unchama.seichiassist.subsystems.mebius.service

import cats.Monad
import com.github.unchama.seichiassist.subsystems.mebius.domain.property.MebiusProperty
import com.github.unchama.seichiassist.subsystems.mebius.domain.speech.{
  MebiusSpeech,
  MebiusSpeechBlockageState,
  MebiusSpeechGateway
}

class MebiusSpeechService[F[_]: Monad](
  gateway: MebiusSpeechGateway[F],
  blockageState: MebiusSpeechBlockageState[F]
) {

  def unblockSpeech(): F[Unit] = blockageState.unblock

  /**
   * `property` をプロパティとして持つMebiusに発話させる。
   *
   * 一度このアクションにてMebiusが発話された場合、 `unblockSpeech` が行われるまで 次の `tryMakingSpeech` は `Monad[F].unit`
   * と等価になる。
   *
   * また、 `unblockSpeech` が事前に呼ばれていたとしても、
   * [[MebiusSpeechBlockageState.speechBlockProbability]]の確率で発話は行われない。
   */
  def tryMakingSpeech(property: MebiusProperty, speech: MebiusSpeech): F[Unit] = {
    import cats.implicits._

    for {
      block <- blockageState.shouldBlock
      _ <-
        if (!block) makeSpeechIgnoringBlockage(property, speech) >> blockageState.block
        else Monad[F].unit
    } yield ()
  }

  /**
   * `property` をプロパティとして持つMebiusに強制的に発話させる。
   */
  def makeSpeechIgnoringBlockage(property: MebiusProperty, speech: MebiusSpeech): F[Unit] = {
    import cats.implicits._

    gateway.sendMessage(property, speech.content) >> gateway.playSpeechSound(speech.strength)
  }

}
