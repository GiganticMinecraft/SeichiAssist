package com.github.unchama.seichiassist.subsystems.mebius.domain.speech

sealed trait MebiusSpeechStrength

object MebiusSpeechStrength {

  case object Medium extends MebiusSpeechStrength

  case object Loud extends MebiusSpeechStrength

}
