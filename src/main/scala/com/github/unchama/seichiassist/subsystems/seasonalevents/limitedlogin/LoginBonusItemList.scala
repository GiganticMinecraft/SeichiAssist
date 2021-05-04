package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

sealed trait LoginBonusIndex

case class EventLoginCount(count: Int) extends LoginBonusIndex {
  require(count > 0, "Login bonus count must be positive")
}

case object Everyday extends LoginBonusIndex

object LoginBonusItemList {
  private val map = Map(
    (EventLoginCount(3), Set(LoginBonus(LoginBonusGachaTicket, 100))),
    (EventLoginCount(9), Set(LoginBonus(LoginBonusGachaTicket, 100))),
    (EventLoginCount(15), Set(LoginBonus(LoginBonusGachaTicket, 200)))
  )
  private val dailyItem: Option[LoginBonus] = None

  def bonusAt(index: LoginBonusIndex): Option[Set[LoginBonus]] = index match {
    case EventLoginCount(count) => map.get(EventLoginCount(count))
    case Everyday => dailyItem.map(bonus => Set(bonus))
  }
}