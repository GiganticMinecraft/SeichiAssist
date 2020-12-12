package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

object LoginBonusItemData {
  private val loginBonusItemMap = Map(
    (0, Set(LoginBonus(LoginBonusGachaTicket, 20))),
    (20, Set(LoginBonus(LoginBonusGachaTicket, 200)))
  )

  def loginBonusAt(day: Int): Option[Set[LoginBonus]] = loginBonusItemMap.get(day)
}