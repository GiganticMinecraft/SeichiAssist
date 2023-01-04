package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

import com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin.LoginBonusDay.{
  Everyday,
  TotalDay
}

object LoginBonusItemList {
  private val map = Map(
    TotalDay(3) -> Set(LoginBonus(LoginBonusGachaTicket, 100)),
    TotalDay(9) -> Set(LoginBonus(LoginBonusGachaTicket, 100)),
    TotalDay(15) -> Set(LoginBonus(LoginBonusGachaTicket, 200))
  )
  private val dailyItem = Set(LoginBonus(LoginBonusGachaTicket, 20))

  def bonusAt(day: LoginBonusDay): Option[Set[LoginBonus]] = day match {
    case TotalDay(count) => map.get(TotalDay(count))
    case Everyday        => Some(dailyItem)
  }
}
