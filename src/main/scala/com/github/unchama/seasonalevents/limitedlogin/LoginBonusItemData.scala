package com.github.unchama.seasonalevents.limitedlogin

object LoginBonusItemData {
  private val loginBonusItemMap = Map(
    (0, LoginBonus(LoginBonusGachaTicket, 20)),
    (20, LoginBonus(LoginBonusGachaTicket, 200))
  )

  def loginBonusAt(day: Int): Option[LoginBonus] = loginBonusItemMap.get(day)
}