package com.github.unchama.seichiassist.mebius.domain

import cats.kernel.Semigroup

trait MebiusSpeechPresentation[Effect] {

  val Effect: Semigroup[Effect]

  def sendMessage(property: MebiusProperty, message: String): Effect

  def playSpeechSound(strength: MebiusSpeechStrength): Effect

  final def speak(speakingMebiusProperty: MebiusProperty, speech: MebiusSpeech): Effect =
    Effect.combine(
      sendMessage(speakingMebiusProperty, speech.content),
      playSpeechSound(speech.strength)
    )

}
