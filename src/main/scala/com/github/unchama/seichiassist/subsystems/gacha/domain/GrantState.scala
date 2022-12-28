package com.github.unchama.seichiassist.subsystems.gacha.domain

sealed trait GrantState

object GrantState {

  case object GrantedMineStack extends GrantState

  case object AddedInventory extends GrantState

  case object Dropped extends GrantState

}
