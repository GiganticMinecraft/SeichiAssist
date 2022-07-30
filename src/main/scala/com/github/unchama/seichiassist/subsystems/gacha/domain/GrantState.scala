package com.github.unchama.seichiassist.subsystems.gacha.domain

sealed trait GrantState

object GrantState {

  case object grantedMineStack extends GrantState

  case object addedInventory extends GrantState

  case object dropped extends GrantState

}
