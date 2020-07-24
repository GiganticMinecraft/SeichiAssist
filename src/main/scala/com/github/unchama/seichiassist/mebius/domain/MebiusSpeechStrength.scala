package com.github.unchama.seichiassist.mebius.domain

sealed trait MebiusSpeechStrength

object MebiusSpeechStrength {

  case object Medium extends MebiusSpeechStrength

  case object Loud extends MebiusSpeechStrength

}
