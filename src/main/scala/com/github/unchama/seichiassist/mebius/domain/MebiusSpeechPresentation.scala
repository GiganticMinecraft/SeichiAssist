package com.github.unchama.seichiassist.mebius.domain

import cats.Monad

trait MebiusSpeechPresentation[F[_]] {

  protected implicit val F: Monad[F]

  import cats.implicits._

  def sendMessage(property: MebiusProperty, message: String): F[Unit]

  def playSpeechSound(strength: MebiusSpeechStrength): F[Unit]

  final def speak(speakingMebiusProperty: MebiusProperty, speech: MebiusSpeech): F[Unit] =
    sendMessage(speakingMebiusProperty, speech.content) >> playSpeechSound(speech.strength)

}
