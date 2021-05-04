package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

object LoginBonusItemData {
  private val loginBonusItemMap = Map(
    (0, LoginBonus(LoginBonusGachaTicket, 20)),
    (3, LoginBonus(LoginBonusGachaTicket, 100)),
    (9, LoginBonus(LoginBonusGachaTicket, 100)),
    (15, LoginBonus(LoginBonusGachaTicket, 200))
  )

  def loginBonusAt(day: Int): Option[LoginBonus] = loginBonusItemMap.get(day)
}