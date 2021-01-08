package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

import enumeratum.{Enum, EnumEntry}

import java.time.LocalDate

sealed trait LimitedLoginEvent extends EnumEntry with LoginBonusItemList with LimitedLoginPeriod

/**
 * イベントの列挙及びそれぞれに必要なものの定義
 */
object LimitedLoginEvents extends Enum[LimitedLoginEvent] {

  val values: IndexedSeq[LimitedLoginEvent] = findValues

  def findActiveEvent: Option[LimitedLoginEvent] = values.find(_.isInEvent)

  case object Valentine extends LimitedLoginEvent {
    override val map = Map(
      (EventLoginCount(20), Set(LoginBonus(LoginBonusGachaTicket, 200)))
    )
    override val dailyItem = Some(LoginBonus(LoginBonusGachaTicket, 20))
    override val period: EventPeriod = EventPeriod(LocalDate.of(2018, 2, 7), LocalDate.of(2018, 2, 21))
  }

}
