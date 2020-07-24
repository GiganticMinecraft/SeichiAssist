package com.github.unchama.seichiassist.mebius.domain

sealed trait MebiusSpeechStrength {
  val speechSoundRepetitionCount: Int
}

object MebiusSpeechStrength {

  case object Medium extends MebiusSpeechStrength {
    override val speechSoundRepetitionCount: Int = 3
  }

  case object Loud extends MebiusSpeechStrength {
    override val speechSoundRepetitionCount: Int = 5
  }

}
