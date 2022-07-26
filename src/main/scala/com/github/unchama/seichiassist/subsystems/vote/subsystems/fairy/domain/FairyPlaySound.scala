package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

sealed trait FairyPlaySound

object FairyPlaySound {

  case object on extends FairyPlaySound

  case object off extends FairyPlaySound

}
