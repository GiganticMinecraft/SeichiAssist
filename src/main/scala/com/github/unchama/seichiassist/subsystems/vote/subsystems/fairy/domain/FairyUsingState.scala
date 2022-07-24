package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

sealed trait FairyUsingState

object FairyUsingState {

  case object Using extends FairyUsingState

  case object NotUsing extends FairyUsingState

}
