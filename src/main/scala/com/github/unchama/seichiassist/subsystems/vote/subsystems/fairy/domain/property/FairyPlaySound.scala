package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

sealed trait FairyPlaySound

object FairyPlaySound {

  case object On extends FairyPlaySound

  case object Off extends FairyPlaySound

}
