package com.github.unchama.seichiassist.mebius.service

import cats.effect.Sync
import com.github.unchama.seichiassist.mebius.domain.property.MebiusProperty
import com.github.unchama.seichiassist.mebius.domain.speech.{MebiusSpeech, MebiusSpeechBlockageState, MebiusSpeechGateway}

class MebiusSpeechService[F[_] : Sync](gateway: MebiusSpeechGateway[F],
                                       blockageState: MebiusSpeechBlockageState[F]) {

  def unblockSpeech(): F[Unit] = blockageState.unblock

  /**
   * `property` をプロパティとして持つMebiusに発話させる。
   *
   * 一度このアクションにてMebiusが発話された場合、 `unblockSpeech` が行われるまで
   * 次の `tryMakingSpeech` は `Monad[F].unit` と等価になる。
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
        else Sync[F].unit
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
