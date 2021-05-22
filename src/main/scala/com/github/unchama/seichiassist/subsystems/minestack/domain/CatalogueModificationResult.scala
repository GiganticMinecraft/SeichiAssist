package com.github.unchama.seichiassist.subsystems.minestack.domain

object CatalogueModificationResult {

  sealed trait OnRegister
  object OnRegister {
    case object Success extends OnRegister
    case class Duplicate(id: MineStackItemId) extends OnRegister
  }

}

