package com.github.unchama.seichiassist.subsystems.tradesystems.domain

sealed trait BigOrRegular

object BigOrRegular {

  case object Big extends BigOrRegular

  case object Regular extends BigOrRegular

}
