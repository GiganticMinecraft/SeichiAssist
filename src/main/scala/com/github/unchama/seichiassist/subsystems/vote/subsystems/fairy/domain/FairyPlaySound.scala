package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

sealed trait FairyPlaySound

object FairyPlaySound {

  case object play extends FairyPlaySound

  case object notPlay extends FairyPlaySound

}
