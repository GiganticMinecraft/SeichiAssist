package com.github.unchama.seichiassist.mebius.domain

import cats.Monad

trait MebiusEffects[F[_]] {

  protected implicit val F: Monad[F]

  import cats.implicits._

  def sendMessage(property: MebiusProperty, message: String): F[Unit]

  def playSpeechSound(strength: MebiusSpeechStrength): F[Unit]

  final def speak(property: MebiusProperty, message: String, speechSoundStrength: MebiusSpeechStrength): F[Unit] =
    sendMessage(property, message) >> playSpeechSound(speechSoundStrength)

}
