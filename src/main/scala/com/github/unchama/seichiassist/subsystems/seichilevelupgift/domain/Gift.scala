package com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain

sealed trait Gift

object Gift {

  sealed trait Item extends Gift

  object Item {

    case object GachaTicket extends Item

    case object SuperPickaxe extends Item

    case object GachaApple extends Item

    case object Elsa extends Item

  }

  case object AutomaticGachaRun extends Gift

}
