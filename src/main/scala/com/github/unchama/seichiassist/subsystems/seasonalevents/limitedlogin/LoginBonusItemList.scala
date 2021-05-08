package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

sealed trait LoginBonusDay

object LoginBonusDay {

  case class TotalDay(totalDay: Int) extends LoginBonusDay {
    require(totalDay > 0, "Total day must be positive")
  }

  case object Everyday extends LoginBonusDay

}

object LoginBonusItemList {

  import LoginBonusDay._

  private val map = Map(
    TotalDay(3) -> Set(LoginBonus(LoginBonusGachaTicket, 100)),
    TotalDay(9) -> Set(LoginBonus(LoginBonusGachaTicket, 100)),
    TotalDay(15) -> Set(LoginBonus(LoginBonusGachaTicket, 200))
  )
  private val dailyItem = Some(LoginBonus(LoginBonusGachaTicket, 20))

  def bonusAt(index: LoginBonusDay): Option[Set[LoginBonus]] = index match {
    case TotalDay(count) => map.get(TotalDay(count))
    case Everyday => dailyItem.map(bonus => Set(bonus))
  }
}
