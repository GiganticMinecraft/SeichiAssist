package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

import java.time.LocalDate

sealed trait LimitedLoginEvent

/**
 * イベントの列挙及びそれぞれに必要なものの定義
 */
object LimitedLoginEvents {
  case object A extends LimitedLoginEvent with LoginBonusItemList with LimitedLoginPeriod {
    override protected val map = Map(
      (0, Set(LoginBonus(LoginBonusGachaTicket, 20))),
      (20, Set(LoginBonus(LoginBonusGachaTicket, 200)))
    )

    override protected val EVENT_PERIOD = EventPeriod(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 31))
  }

  case object B extends LimitedLoginEvent with LoginBonusItemList with LimitedLoginPeriod {
    override protected val map = Map(
      (0, Set(LoginBonus(LoginBonusGachaTicket, 20))),
      (20, Set(LoginBonus(LoginBonusGachaTicket, 200)))
    )

    override protected val EVENT_PERIOD = EventPeriod(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 31))
  }
}