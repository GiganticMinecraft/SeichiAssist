package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

sealed trait LoginBonusDay

object LoginBonusDay {

  case class TotalDay(totalDay: Int) extends LoginBonusDay {
    require(totalDay > 0, "Total day must be positive")
  }

  case object Everyday extends LoginBonusDay

}
