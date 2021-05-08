package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

sealed trait LoginBonusDay

case class EventLoginCount(count: Int) extends LoginBonusDay {
  require(count > 0, "Login bonus count must be positive")
}

case object Everyday extends LoginBonusDay

object LoginBonusItemList {
  private val map = Map(
    EventLoginCount(3) -> Set(LoginBonus(LoginBonusGachaTicket, 100)),
    EventLoginCount(9) -> Set(LoginBonus(LoginBonusGachaTicket, 100)),
    EventLoginCount(15) -> Set(LoginBonus(LoginBonusGachaTicket, 200))
  )
  private val dailyItem = Some(LoginBonus(LoginBonusGachaTicket, 20))

  def bonusAt(index: LoginBonusDay): Option[Set[LoginBonus]] = index match {
    case EventLoginCount(count) => map.get(EventLoginCount(count))
    case Everyday => dailyItem.map(bonus => Set(bonus))
  }
}
