package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

sealed trait LoginBonusIndex

case class EventLoginCount(count: Int) extends LoginBonusIndex {
  require(count > 0, "Login bonus count must be positive")
}

case object Everyday extends LoginBonusIndex

trait LoginBonusItemList {
  val map: Map[EventLoginCount, Set[LoginBonus]]
  val dailyItem: Option[LoginBonus] = None

  final def bonusAt(index: LoginBonusIndex): Option[Set[LoginBonus]] = index match {
    case EventLoginCount(count) => map.get(EventLoginCount(count))
    case Everyday => dailyItem.flatMap(bonus => Some(Set(bonus)))
  }
}